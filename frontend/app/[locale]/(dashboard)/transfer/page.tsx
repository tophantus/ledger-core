"use client";

import {
    ArrowLeft,
    ArrowRight,
    CheckCircle2,
    Loader2,
} from "lucide-react";
import {useCallback, useEffect, useState} from "react";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useTranslations} from "next-intl";

import {Link, useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {Button} from "@/components/ui/button";

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

type TransferStep =
    | "SOURCE"
    | "DESTINATION"
    | "DETAILS"
    | "OTP"
    | "RESULT";

export default function TransferPage() {
    const t = useTranslations("transaction");
    const tErrors = useTranslations("errors");

    const router = useRouter();

    const {getMyAccounts} = useMyAccounts();
    const {getAccountHolder} = useAccountHolder();
    const {
        createTransferIntent,
    } = useCreateTransferIntent();
    const {
        confirmTransfer,
    } = useConfirmTransfer();

    const [step, setStep] =
        useState<TransferStep>("SOURCE");

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
                reference: "",
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

    const getErrorMessage = useCallback((
        code?: string,
    ): string => {
        if (
            code &&
            tErrors.has(code)
        ) {
            return tErrors(code);
        }

        return tErrors("fallback");
    }, [tErrors]);

    useEffect(() => {
        let mounted = true;

        const loadAccounts = async () => {
            setIsAccountsLoading(true);

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

                setAccounts(response.data);
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
    }, [getErrorMessage, getMyAccounts, t, tErrors]);

    const handleSelectSource = (
        account: AccountSummary,
    ) => {
        setSelectedAccount(account);
        setError(null);
        setStep("DESTINATION");
    };

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
                    setStep("DETAILS");
                } catch {
                    setError(
                        tErrors("fallback"),
                    );
                } finally {
                    setIsHolderLoading(false);
                }
            },
        );

    const handleCreateIntent =
        detailsForm.handleSubmit(
            async (values) => {
                if (!selectedAccount || !holder) {
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
                            reference:
                            values.reference,
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
                            intentId: intent.intentId,
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

                    setTransaction(response.data);
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

    const handleBackToDestination = () => {
        setHolder(null);
        setError(null);
        setStep("DESTINATION");
    };

    const handleBackToSource = () => {
        setSelectedAccount(null);
        setHolder(null);
        setError(null);
        setStep("SOURCE");
    };

    const handleBackToDetails = () => {
        setError(null);
        setStep("DETAILS");
    };

    const handleStartNewTransfer = () => {
        setSelectedAccount(null);
        setHolder(null);
        setIntent(null);
        setTransaction(null);
        setError(null);

        destinationForm.reset();
        detailsForm.reset();
        otpForm.reset();

        setStep("SOURCE");
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
                        {t("transfer.description")}
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

            {step === "SOURCE" && (
                <div className="rounded-lg border border-border bg-surface p-6">
                    <h2 className="text-base font-semibold text-foreground">
                        {t(
                            "transfer.selectSource",
                        )}
                    </h2>

                    <div className="mt-4 space-y-3">
                        {isAccountsLoading && (
                            <div className="flex items-center justify-center py-8">
                                <Loader2 className="h-5 w-5 animate-spin text-muted" />
                            </div>
                        )}

                        {!isAccountsLoading &&
                            accounts.length === 0 && (
                                <p className="py-8 text-center text-sm text-muted">
                                    {t(
                                        "transfer.noAccounts",
                                    )}
                                </p>
                            )}

                        {!isAccountsLoading &&
                            accounts.filter(a => a.status === "ACTIVE").map(
                                (account) => (
                                    <button
                                        key={account.id}
                                        type="button"
                                        onClick={() =>
                                            handleSelectSource(
                                                account,
                                            )
                                        }
                                        disabled={
                                            account.status !==
                                            "ACTIVE"
                                        }
                                        className="
                                            flex
                                            w-full
                                            items-center
                                            justify-between
                                            rounded-lg
                                            border
                                            border-border
                                            p-4
                                            text-left
                                            transition
                                            hover:border-primary
                                            hover:bg-background-subtle
                                            disabled:cursor-not-allowed
                                            disabled:opacity-50
                                        "
                                    >
                                        <div>
                                            <p className="text-sm font-medium text-primary">
                                                {
                                                    account.accountNo
                                                }
                                            </p>

                                            <p className="mt-1 text-xs text-muted">
                                                {
                                                    account.currency
                                                }
                                            </p>
                                        </div>

                                        <div className="text-right">
                                            <p className="text-sm font-semibold text-primary">
                                                {
                                                    account.balance
                                                }{" "}
                                                {
                                                    account.currency
                                                }
                                            </p>

                                            <p className="mt-1 text-xs text-muted">
                                                {
                                                    account.status
                                                }
                                            </p>
                                        </div>
                                    </button>
                                ),
                            )}
                    </div>
                </div>
            )}

            {step === "DESTINATION" &&
                selectedAccount && (
                    <form
                        onSubmit={
                            handleFindHolder
                        }
                        className="rounded-lg border border-border bg-surface p-6"
                    >
                        <div className="rounded-md bg-background-subtle p-4">
                            <p className="text-xs text-muted">
                                {t(
                                    "transfer.sourceAccount",
                                )}
                            </p>

                            <p className="mt-1 text-sm font-medium text-primary">
                                {
                                    selectedAccount.accountNo
                                }
                            </p>

                            <p className="mt-1 text-xs text-muted">
                                {
                                    selectedAccount.balance
                                }{" "}
                                {
                                    selectedAccount.currency
                                }
                            </p>
                        </div>

                        <div className="mt-6">
                            <label
                                htmlFor="destination-account-no"
                                className="mb-2 block text-sm font-medium text-foreground"
                            >
                                {t(
                                    "transfer.destinationAccount",
                                )}
                            </label>

                            <input
                                id="destination-account-no"
                                {...destinationForm.register(
                                    "destinationAccountNo",
                                )}
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2.5
                                    text-sm
                                    text-foreground
                                    outline-none
                                "
                                placeholder={t(
                                    "transfer.destinationPlaceholder",
                                )}
                            />

                            {destinationForm.formState
                                .errors
                                .destinationAccountNo && (
                                <p className="mt-1 text-xs text-danger">
                                    {
                                        destinationForm
                                            .formState
                                            .errors
                                            .destinationAccountNo
                                            .message
                                    }
                                </p>
                            )}
                        </div>

                        <div className="mt-6 flex justify-between gap-3">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={
                                    handleBackToSource
                                }
                            >
                                {t("back")}
                            </Button>

                            <Button
                                type="submit"
                                loading={
                                    isHolderLoading
                                }
                            >
                                {t(
                                    "transfer.continue",
                                )}
                                <ArrowRight className="h-4 w-4" />
                            </Button>
                        </div>
                    </form>
                )}

            {step === "DETAILS" &&
                selectedAccount &&
                holder && (
                    <form
                        onSubmit={
                            handleCreateIntent
                        }
                        className="rounded-lg border border-border bg-surface p-6"
                    >
                        <div className="rounded-lg border border-border bg-background-subtle p-4 mb-3">
                            <p className="text-xs text-muted">
                                {t("transfer.sourceAccount")}
                            </p>

                            <div className="mt-1 flex items-center justify-between gap-4">
                                <div>
                                    <p className="text-sm font-semibold text-primary">
                                        {selectedAccount.accountNo}
                                    </p>

                                    <p className="mt-1 text-xs text-muted">
                                        {selectedAccount.currency}
                                    </p>
                                </div>

                                <div className="text-right">
                                    <p className="text-xs text-muted">
                                        {t(
                                            "transfer.availableBalance",
                                        )}
                                    </p>

                                    <p className="mt-1 text-sm font-semibold text-primary">
                                        {selectedAccount.balance}{" "}
                                        {selectedAccount.currency}
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div className="rounded-lg border border-border bg-background-subtle p-4">
                            <p className="text-xs text-muted">
                                {t(
                                    "transfer.recipient",
                                )}
                            </p>

                            <p className="mt-1 text-sm font-semibold text-primary">
                                {
                                    holder.fullName
                                }
                            </p>

                            <p className="mt-1 text-xs text-muted">
                                {
                                    holder.accountNo
                                }
                            </p>
                        </div>

                        <div className="mt-6 space-y-5">
                            <div>
                                <label
                                    htmlFor="transfer-amount"
                                    className="mb-2 block text-sm font-medium text-foreground"
                                >
                                    {t(
                                        "transfer.amount",
                                    )}
                                </label>

                                <div className="relative">
                                    <input
                                        id="transfer-amount"
                                        type="text"
                                        inputMode="decimal"
                                        {...detailsForm.register(
                                            "amount",
                                        )}
                                        className="
                                            w-full
                                            rounded-md
                                            border
                                            border-border
                                            bg-background
                                            px-3
                                            py-2.5
                                            pr-16
                                            text-sm
                                            text-primary
                                            outline-none
                                        "
                                        placeholder="0.00"
                                    />

                                    <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-medium text-muted">
                                        {
                                            selectedAccount.currency
                                        }
                                    </span>
                                </div>

                                {detailsForm.formState
                                    .errors.amount && (
                                    <p className="mt-1 text-xs text-danger">
                                        {
                                            detailsForm
                                                .formState
                                                .errors.amount
                                                .message
                                        }
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="transfer-reference"
                                    className="mb-2 block text-sm font-medium text-foreground"
                                >
                                    {t(
                                        "transfer.reference",
                                    )}
                                </label>

                                <input
                                    id="transfer-reference"
                                    {...detailsForm.register(
                                        "reference",
                                    )}
                                    className="
                                        w-full
                                        rounded-md
                                        border
                                        border-border
                                        bg-background
                                        px-3
                                        py-2.5
                                        text-sm
                                        text-foreground
                                        outline-none
                                    "
                                    placeholder={t(
                                        "transfer.referencePlaceholder",
                                    )}
                                />

                                {detailsForm.formState
                                    .errors.reference && (
                                    <p className="mt-1 text-xs text-danger">
                                        {
                                            detailsForm
                                                .formState
                                                .errors
                                                .reference
                                                .message
                                        }
                                    </p>
                                )}
                            </div>

                            <div>
                                <label
                                    htmlFor="transfer-description"
                                    className="mb-2 block text-sm font-medium text-foreground"
                                >
                                    {t(
                                        "transfer.descriptionField",
                                    )}
                                </label>

                                <textarea
                                    id="transfer-description"
                                    rows={3}
                                    {...detailsForm.register(
                                        "description",
                                    )}
                                    className="
                                        w-full
                                        resize-none
                                        rounded-md
                                        border
                                        border-border
                                        bg-background
                                        px-3
                                        py-2.5
                                        text-sm
                                        text-foreground
                                        outline-none
                                    "
                                    placeholder={t(
                                        "transfer.descriptionPlaceholder",
                                    )}
                                />

                                {detailsForm.formState
                                    .errors
                                    .description && (
                                    <p className="mt-1 text-xs text-danger">
                                        {
                                            detailsForm
                                                .formState
                                                .errors
                                                .description
                                                .message
                                        }
                                    </p>
                                )}
                            </div>
                        </div>

                        <div className="mt-6 flex justify-between gap-3">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={
                                    handleBackToDestination
                                }
                            >
                                {t("back")}
                            </Button>

                            <Button
                                type="submit"
                                loading={
                                    isIntentLoading
                                }
                            >
                                {t(
                                    "transfer.review",
                                )}
                                <ArrowRight className="h-4 w-4" />
                            </Button>
                        </div>
                    </form>
                )}

            {step === "OTP" &&
                intent &&
                holder && (
                    <form
                        onSubmit={
                            handleConfirmTransfer
                        }
                        className="rounded-lg border border-border bg-surface p-6"
                    >
                        <div>
                            <h2 className="text-base font-semibold text-primary">
                                {t(
                                    "transfer.confirmTitle",
                                )}
                            </h2>

                            <p className="mt-1 text-sm text-muted">
                                {t(
                                    "transfer.confirmDescription",
                                )}
                            </p>
                        </div>

                        <div className="mt-6 rounded-lg bg-background-subtle p-4">
                            <div className="flex items-center justify-between gap-4">
                                <div>
                                    <p className="text-xs text-muted">
                                        {t(
                                            "transfer.recipient",
                                        )}
                                    </p>

                                    <p className="mt-1 text-sm font-medium text-primary">
                                        {
                                            holder.fullName
                                        }
                                    </p>

                                    <p className="mt-1 text-xs text-muted">
                                        {
                                            holder.accountNo
                                        }
                                    </p>
                                </div>

                                <div className="text-right">
                                    <p className="text-xs text-muted">
                                        {t(
                                            "transfer.amount",
                                        )}
                                    </p>

                                    <p className="mt-1 text-sm font-semibold text-primary">
                                        {
                                            intent.amount
                                        }{" "}
                                        {
                                            intent.currency
                                        }
                                    </p>
                                </div>
                            </div>

                            <div className="mt-4 border-t border-border pt-4">
                                <p className="text-xs text-muted">
                                    {t(
                                        "transfer.reference",
                                    )}
                                </p>

                                <p className="mt-1 text-sm text-primary">
                                    {
                                        intent.reference
                                    }
                                </p>
                            </div>
                        </div>

                        <div className="mt-6">
                            <label
                                htmlFor="transfer-otp"
                                className="mb-2 block text-sm font-medium text-foreground"
                            >
                                {t(
                                    "transfer.otp",
                                )}
                            </label>

                            <input
                                id="transfer-otp"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                {...otpForm.register(
                                    "otp",
                                )}
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2.5
                                    text-sm
                                    tracking-[0.3em]
                                    text-primary
                                    outline-none
                                "
                                placeholder={t(
                                    "transfer.otpPlaceholder",
                                )}
                            />

                            {otpForm.formState
                                .errors.otp && (
                                <p className="mt-1 text-xs text-danger">
                                    {
                                        otpForm
                                            .formState
                                            .errors
                                            .otp
                                            .message
                                    }
                                </p>
                            )}
                        </div>

                        <div className="mt-6 flex justify-between gap-3">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={
                                    handleBackToDetails
                                }
                            >
                                {t("back")}
                            </Button>

                            <Button
                                type="submit"
                                loading={
                                    isConfirmLoading
                                }
                            >
                                {t(
                                    "transfer.confirm",
                                )}
                            </Button>
                        </div>
                    </form>
                )}

            {step === "RESULT" &&
                transaction && (
                    <div className="rounded-lg border border-border bg-surface p-6">
                        <div className="flex flex-col items-center text-center">
                            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-success">
                                <CheckCircle2 className="h-7 w-7 text-success-foreground" />
                            </div>

                            <h2 className="mt-4 text-xl font-semibold text-success">
                                {t(
                                    "transfer.successTitle",
                                )}
                            </h2>

                            <p className="mt-1 text-sm text-muted">
                                {t(
                                    "transfer.successDescription",
                                )}
                            </p>
                        </div>

                        <div className="mt-6 divide-y divide-border rounded-lg border">
                            <ResultRow
                                label={t(
                                    "transfer.transactionId",
                                )}
                                value={
                                    transaction.id
                                }
                            />

                            <ResultRow
                                label={t(
                                    "transfer.reference",
                                )}
                                value={
                                    transaction.reference
                                }
                            />

                            <ResultRow
                                label={t(
                                    "transfer.amount",
                                )}
                                value={`${transaction.amount} ${transaction.currency}`}
                            />

                            <ResultRow
                                label={t(
                                    "transfer.status",
                                )}
                                value={t(
                                    `statuses.${transaction.status}`,
                                )}
                            />
                        </div>

                        <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() =>
                                    router.push(
                                        ROUTES.TRANSACTION.LIST,
                                    )
                                }
                            >
                                {t(
                                    "transfer.viewTransactions",
                                )}
                            </Button>

                            <Button
                                type="button"
                                onClick={
                                    handleStartNewTransfer
                                }
                            >
                                {t(
                                    "transfer.newTransfer",
                                )}
                            </Button>
                        </div>
                    </div>
                )}
        </section>
    );
}

function TransferProgress({
                              step,
                          }: {
    step: TransferStep;
}) {
    const t = useTranslations("transaction");

    const steps: Array<{
        key: TransferStep;
        label: string;
    }> = [
        {
            key: "SOURCE",
            label: t(
                "transfer.steps.source",
            ),
        },
        {
            key: "DESTINATION",
            label: t(
                "transfer.steps.destination",
            ),
        },
        {
            key: "DETAILS",
            label: t(
                "transfer.steps.details",
            ),
        },
        {
            key: "OTP",
            label: t(
                "transfer.steps.confirm",
            ),
        },
    ];

    const currentIndex = steps.findIndex(
        (item) => item.key === step,
    );

    return (
        <div className="grid grid-cols-4 gap-2">
            {steps.map((item, index) => {
                const active =
                    index <= currentIndex;

                return (
                    <div key={item.key}>
                        <div
                            className={`
                                h-1
                                rounded-full
                                ${
                                active
                                    ? "bg-primary"
                                    : "bg-secondary"
                            }
                            `}
                        />

                        <p
                            className={`
                                mt-2
                                text-xs
                                ${
                                active
                                    ? "text-primary"
                                    : "text-muted"
                            }
                            `}
                        >
                            {item.label}
                        </p>
                    </div>
                );
            })}
        </div>
    );
}

function ResultRow({
                       label,
                       value,
                   }: {
    label: string;
    value: string;
}) {
    return (
        <div className="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
            <span className="text-xs text-muted">
                {label}
            </span>

            <span className="break-all text-sm font-medium text-primary sm:text-right">
                {value}
            </span>
        </div>
    );
}