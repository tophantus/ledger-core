"use client";

import {
    ArrowLeft,
} from "lucide-react";
import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {useLocale, useTranslations} from "next-intl";
import {useSearchParams} from "next/navigation";

import {Link, useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";
import {useAccountHolder} from "@/features/account/hooks/use-account-holder";
import type {
    AccountHolder,
    AccountSummary,
} from "@/features/account/types/account";

import {useCreateTransferIntent} from "@/features/transaction/hooks/use-create-transfer-intent";
import {useConfirmTransfer} from "@/features/transaction/hooks/use-confirm-transfer";

import type {
    CreateTransferIntentResult,
    Transaction,
} from "@/features/transaction/types/transaction";

import {
    transferDestinationSchema,
    transferDetailsSchema,
    transferOtpSchema,
    type TransferDestinationForm,
    type TransferDetailsForm,
    type TransferOtpForm,
} from "@/features/transaction/schemas/transfer-schema";

import {isAmountLessThanOrEqual} from "@/lib/utils/money";
import {generateTransactionReference} from "@/lib/utils/reference";

import {TransferProgress} from "./transfer-progress";
import {TransferForm} from "./transfer-form";
import {TransferConfirmation} from "./transfer-confirmation";
import {TransferResult} from "./transfer-result";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";

type TransferStep =
    | "TRANSFER"
    | "OTP"
    | "RESULT";

export default function TransferPageContent() {
    const t = useTranslations("transaction");
    const tErrors = useTranslations("errors");
    const locale = useLocale();

    const router = useRouter();
    const searchParams = useSearchParams();

    const urlAccountNo =
        searchParams.get("accountNo")?.trim();

    const urlReference =
        searchParams.get("reference")?.trim();

    const [reference, setReference] =
        useState(
            () =>
                urlReference ||
                generateTransactionReference(),
        );

    const {getMyAccounts} = useMyAccounts();
    const {getAccountHolder} = useAccountHolder();

    const {
        createTransferIntent,
    } = useCreateTransferIntent();

    const {
        confirmTransfer,
    } = useConfirmTransfer();

    const [step, setStep] =
        useState<TransferStep>("TRANSFER");

    const [accounts, setAccounts] = useState<
        AccountSummary[]
    >([]);

    const [selectedAccount, setSelectedAccount] =
        useState<AccountSummary | null>(null);

    const [holder, setHolder] =
        useState<AccountHolder | null>(null);

    const [intent, setIntent] =
        useState<CreateTransferIntentResult | null>(
            null,
        );

    const [transaction, setTransaction] =
        useState<Transaction | null>(null);

    const [isAccountsLoading, setIsAccountsLoading] =
        useState(true);

    const [isHolderLoading, setIsHolderLoading] =
        useState(false);

    const [isIntentLoading, setIsIntentLoading] =
        useState(false);

    const [isConfirmLoading, setIsConfirmLoading] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const destinationForm =
        useForm<TransferDestinationForm>({
            resolver: zodResolver(
                transferDestinationSchema,
            ),
            defaultValues: {
                destinationAccountNo: "",
            },
        });

    const detailsForm =
        useForm<TransferDetailsForm>({
            resolver: zodResolver(
                transferDetailsSchema,
            ),
            defaultValues: {
                amount: "",
                description: "",
            },
        });

    const otpForm =
        useForm<TransferOtpForm>({
            resolver: zodResolver(
                transferOtpSchema,
            ),
            defaultValues: {
                otp: "",
            },
        });

    const getErrorMessage = useCallback(
        (code?: string): string => {
            if (
                code &&
                tErrors.has(code)
            ) {
                return tErrors(code);
            }

            return tErrors("fallback");
        },
        [tErrors],
    );

    /*
     * Load holder from URL.
     */
    useEffect(() => {
        if (!urlAccountNo) {
            return;
        }

        let mounted = true;

        const loadHolder = async () => {
            setIsHolderLoading(true);
            setError(null);
            setHolder(null);

            destinationForm.setValue(
                "destinationAccountNo",
                urlAccountNo,
            );

            try {
                const response =
                    await getAccountHolder(
                        urlAccountNo,
                    );

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                setHolder(response.data);
            } catch {
                if (mounted) {
                    setError(
                        tErrors("fallback"),
                    );
                }
            } finally {
                if (mounted) {
                    setIsHolderLoading(false);
                }
            }
        };

        void loadHolder();

        return () => {
            mounted = false;
        };
    }, [
        urlAccountNo,
        getAccountHolder,
        getErrorMessage,
        destinationForm,
        tErrors,
    ]);

    /*
     * Load active accounts.
     */
    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            try {
                const response =
                    await getMyAccounts();

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setError(
                        getErrorMessage(
                            response.code,
                        ),
                    );

                    return;
                }

                const activeAccounts =
                    response.data.filter(
                        (account) =>
                            account.status ===
                            "ACTIVE",
                    );

                setAccounts(activeAccounts);

                setSelectedAccount(
                    activeAccounts[0] ?? null,
                );
            } catch {
                if (mounted) {
                    setError(
                        tErrors("fallback"),
                    );
                }
            } finally {
                if (mounted) {
                    setIsAccountsLoading(false);
                }
            }
        };

        void loadAccounts();

        return () => {
            mounted = false;
        };
    }, [
        getErrorMessage,
        getMyAccounts,
        tErrors,
    ]);

    const handleFindHolder =
        destinationForm.handleSubmit(
            async (values) => {
                setIsHolderLoading(true);
                setError(null);
                setHolder(null);

                try {
                    const response =
                        await getAccountHolder(
                            values.destinationAccountNo,
                        );

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setHolder(response.data);
                } catch {
                    setError(
                        tErrors("fallback"),
                    );
                } finally {
                    setIsHolderLoading(false);
                }
            },
        );

    const handleChangeDestination = () => {
        setHolder(null);
        setError(null);

        destinationForm.reset({
            destinationAccountNo: "",
        });
    };

    const handleCreateIntent =
        detailsForm.handleSubmit(
            async (values) => {
                if (
                    !selectedAccount ||
                    !holder
                ) {
                    return;
                }

                if (
                    !isAmountLessThanOrEqual(
                        values.amount,
                        selectedAccount.balance,
                    )
                ) {
                    setError(
                        t(
                            "transfer.amountExceedsBalance",
                        ),
                    );

                    return;
                }

                setIsIntentLoading(true);
                setError(null);

                try {
                    const response =
                        await createTransferIntent({
                            sourceAccountId:
                            selectedAccount.id,
                            destinationAccountNo:
                            holder.accountNo,
                            amount: values.amount,
                            currency:
                            selectedAccount.currency,
                            reference,
                            description:
                                values.description ||
                                undefined,
                        });

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setIntent(response.data);
                    setStep("OTP");
                } catch {
                    setError(
                        tErrors("fallback"),
                    );
                } finally {
                    setIsIntentLoading(false);
                }
            },
        );

    const handleConfirmTransfer =
        otpForm.handleSubmit(
            async (values) => {
                if (!intent) {
                    return;
                }

                setIsConfirmLoading(true);
                setError(null);

                try {
                    const response =
                        await confirmTransfer({
                            intentId:
                            intent.intentId,
                            otp: values.otp,
                        });

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setTransaction(
                        response.data,
                    );

                    setStep("RESULT");
                } catch {
                    setError(
                        tErrors("fallback"),
                    );
                } finally {
                    setIsConfirmLoading(false);
                }
            },
        );

    const handleStartNewTransfer = () => {
        setSelectedAccount(
            accounts[0] ?? null,
        );

        setHolder(null);
        setIntent(null);
        setTransaction(null);
        setError(null);

        setReference(
            generateTransactionReference(),
        );

        destinationForm.reset({
            destinationAccountNo: "",
        });

        detailsForm.reset({
            amount: "",
            description: "",
        });

        otpForm.reset({
            otp: "",
        });

        setStep("TRANSFER");

        router.replace(
            ROUTES.TRANSACTION.TRANSFER,
        );
    };

    const handleAccountChange = (
        account: AccountSummary,
    ) => {
        setSelectedAccount(account);
        detailsForm.setValue(
            "amount",
            "",
        );
        setError(null);
    };

    return (
        <section className="mx-auto max-w-2xl space-y-3">
            <div className="space-y-2">
                <Link
                    href={ROUTES.DASHBOARD}
                    className="
                        inline-flex
                        items-center
                        gap-2
                        text-sm
                        text-muted
                        hover:text-primary
                    "
                >
                    <ArrowLeft className="h-4 w-4" />
                    {t("back")}
                </Link>

                <div>
                    <h1 className="text-2xl font-semibold text-primary">
                        {t("transfer.title")}
                    </h1>

                    <p className="mt-1 text-sm text-muted">
                        {t(
                            "transfer.description",
                        )}
                    </p>
                </div>
            </div>

            {step !== "RESULT" && (
                <TransferProgress
                    step={step}
                />
            )}

            {error && (
                <div className="rounded-lg border border-danger bg-surface p-4">
                    <p className="text-sm text-danger">
                        {error}
                    </p>
                </div>
            )}

            {step === "TRANSFER" && (
                <TransferForm
                    accounts={accounts}
                    selectedAccount={selectedAccount}
                    holder={holder}
                    isAccountsLoading={
                        isAccountsLoading
                    }
                    isHolderLoading={
                        isHolderLoading
                    }
                    isIntentLoading={
                        isIntentLoading
                    }
                    destinationForm={
                        destinationForm
                    }
                    detailsForm={detailsForm}
                    onAccountChange={
                        handleAccountChange
                    }
                    onFindHolder={
                        handleFindHolder
                    }
                    onChangeDestination={
                        handleChangeDestination
                    }
                    onCreateIntent={
                        handleCreateIntent
                    }
                />
            )}

            {step === "OTP" &&
                intent &&
                holder && (
                    <TransferConfirmation
                        intent={intent}
                        holder={holder}
                        selectedAccount={
                            selectedAccount
                        }
                        otpForm={otpForm}
                        isConfirmLoading={
                            isConfirmLoading
                        }
                        onBack={() =>
                            setStep("TRANSFER")
                        }
                        onConfirm={
                            handleConfirmTransfer
                        }
                    />
                )}

            {step === "RESULT" &&
                transaction && (
                    <TransferResult
                        transaction={
                            transaction
                        }
                        locale={locale}
                        onViewTransactions={() =>
                            router.push(
                                ROUTES
                                    .TRANSACTION
                                    .LIST,
                            )
                        }
                        onNewTransfer={
                            handleStartNewTransfer
                        }
                    />
                )}
        </section>
    );
}