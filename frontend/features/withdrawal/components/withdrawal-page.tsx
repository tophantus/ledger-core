"use client";

import {
    ArrowLeft,
} from "lucide-react";
import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {Link, useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";

import {useCreateWithdrawalRequest} from "../hooks/use-create-withdrawal-request";
import {useConfirmWithdrawalRequest} from "../hooks/use-confirm-withdrawal-request";

import type {
    AccountSummary,
} from "@/features/account/types/account";

import type {
    ConfirmWithdrawalRequestResponse,
    WithdrawalRequestResponse,
} from "../types/withdrawal";

import {
    withdrawalOtpSchema,
    withdrawalSchema,
    type WithdrawalFormValues,
    type WithdrawalOtpForm,
} from "../schemas/withdrawal-schema";

import {isAmountLessThanOrEqual} from "@/lib/utils/money";

import {WithdrawalProgress} from "./withdrawal-progress";
import {WithdrawalForm} from "./withdrawal-form";
import {WithdrawalConfirmation} from "./withdrawal-confirmation";
import {WithdrawalResult} from "./withdrawal-result";

import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";

type WithdrawalStep =
    | "WITHDRAWAL"
    | "OTP"
    | "RESULT";

export default function WithdrawalPageContent() {
    const t = useTranslations("withdrawal");
    const tErrors = useTranslations("errors");

    const router = useRouter();

    const {getMyAccounts} =
        useMyAccounts();

    const {
        createWithdrawalRequest,
    } =
        useCreateWithdrawalRequest();

    const {
        confirmWithdrawalRequest,
    } =
        useConfirmWithdrawalRequest();

    const [step, setStep] =
        useState<WithdrawalStep>(
            "WITHDRAWAL",
        );

    const [accounts, setAccounts] =
        useState<AccountSummary[]>([]);

    const [
        selectedAccount,
        setSelectedAccount,
    ] =
        useState<AccountSummary | null>(
            null,
        );

    const [request, setRequest] =
        useState<WithdrawalRequestResponse | null>(
            null,
        );

    const [result, setResult] =
        useState<ConfirmWithdrawalRequestResponse | null>(
            null,
        );

    const [
        isAccountsLoading,
        setIsAccountsLoading,
    ] = useState(true);

    const [
        isCreateLoading,
        setIsCreateLoading,
    ] = useState(false);

    const [
        isConfirmLoading,
        setIsConfirmLoading,
    ] = useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const form =
        useForm<WithdrawalFormValues>({
            resolver: zodResolver(
                withdrawalSchema,
            ),
            defaultValues: {
                amount: "",
            },
        });

    const otpForm =
        useForm<WithdrawalOtpForm>({
            resolver: zodResolver(
                withdrawalOtpSchema,
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

                setAccounts(
                    activeAccounts,
                );

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
                    setIsAccountsLoading(
                        false,
                    );
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

    const handleCreateRequest =
        form.handleSubmit(
            async (values) => {
                if (!selectedAccount) {
                    return;
                }

                if (
                    !isAmountLessThanOrEqual(
                        values.amount,
                        selectedAccount.availableBalance,
                    )
                ) {
                    setError(
                        t(
                            "amountExceedsBalance",
                        ),
                    );

                    return;
                }

                setIsCreateLoading(true);
                setError(null);

                try {
                    const response =
                        await createWithdrawalRequest(
                            {
                                accountId:
                                selectedAccount.id,
                                amount:
                                values.amount,
                                currency:
                                selectedAccount.currency,
                            },
                        );

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setRequest(
                        response.data,
                    );

                    otpForm.reset({
                        otp: "",
                    });

                    setStep("OTP");
                } catch {
                    setError(
                        tErrors("fallback"),
                    );
                } finally {
                    setIsCreateLoading(
                        false,
                    );
                }
            },
        );

    const handleConfirmWithdrawal =
        otpForm.handleSubmit(
            async (values) => {
                if (!request) {
                    return;
                }

                setIsConfirmLoading(true);
                setError(null);

                try {
                    const response =
                        await confirmWithdrawalRequest(
                            request.requestId,
                            {
                                otp: values.otp,
                            },
                        );

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setResult(
                        response.data,
                    );

                    setStep("RESULT");
                } catch {
                    setError(
                        tErrors("fallback"),
                    );
                } finally {
                    setIsConfirmLoading(
                        false,
                    );
                }
            },
        );

    const handleAccountChange = (
        account: AccountSummary,
    ) => {
        setSelectedAccount(account);

        form.setValue(
            "amount",
            "",
        );

        form.clearErrors("amount");
        setError(null);
    };

    const handleBack = () => {
        setStep("WITHDRAWAL");
        setError(null);

        otpForm.reset({
            otp: "",
        });
    };

    const handleStartNewWithdrawal =
        () => {
            setSelectedAccount(
                accounts[0] ?? null,
            );

            setRequest(null);
            setResult(null);
            setError(null);

            form.reset({
                amount: "",
            });

            otpForm.reset({
                otp: "",
            });

            setStep("WITHDRAWAL");

            router.replace(
                ROUTES.WITHDRAWAL,
            );
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
                        {t(
                            "title",
                        )}
                    </h1>

                    <p className="mt-1 text-sm text-muted">
                        {t(
                            "description",
                        )}
                    </p>
                </div>
            </div>

            {step !== "RESULT" && (
                <WithdrawalProgress
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

            {step === "WITHDRAWAL" && (
                <WithdrawalForm
                    accounts={accounts}
                    selectedAccount={
                        selectedAccount
                    }
                    isAccountsLoading={
                        isAccountsLoading
                    }
                    isCreateLoading={
                        isCreateLoading
                    }
                    form={form}
                    onAccountChange={
                        handleAccountChange
                    }
                    onCreateRequest={
                        handleCreateRequest
                    }
                />
            )}

            {step === "OTP" &&
                request && (
                    <WithdrawalConfirmation
                        request={request}
                        selectedAccount={
                            selectedAccount
                        }
                        otpForm={otpForm}
                        isConfirmLoading={
                            isConfirmLoading
                        }
                        onBack={handleBack}
                        onConfirm={
                            handleConfirmWithdrawal
                        }
                    />
                )}

            {step === "RESULT" &&
                result && (
                    <WithdrawalResult
                        result={result}
                        onNewWithdrawal={
                            handleStartNewWithdrawal
                        }
                    />
                )}
        </section>
    );
}