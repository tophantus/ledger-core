"use client";

import {ArrowLeft,} from "lucide-react";
import {useCallback, useEffect, useState,} from "react";
import {useTranslations} from "next-intl";

import {Link, useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";

import {useCreateWithdrawalRequest} from "../hooks/use-create-withdrawal-request";
import {useConfirmWithdrawalRequest} from "../hooks/use-confirm-withdrawal-request";

import {AccountStatus, AccountSummary,} from "@/features/account/types/account";

import type {ConfirmWithdrawalRequestResponse, WithdrawalRequestResponse,} from "../types/withdrawal";

import {
    type WithdrawalFormValues,
    type WithdrawalOtpForm,
    withdrawalOtpSchema,
    withdrawalSchema,
} from "../schemas/withdrawal-schema";

import {isAmountGreaterThanZero, isAmountLessThanOrEqual} from "@/lib/utils/money";

import {WithdrawalProgress} from "./withdrawal-progress";
import {WithdrawalForm} from "./withdrawal-form";
import {WithdrawalConfirmation} from "./withdrawal-confirmation";
import {WithdrawalResult} from "./withdrawal-result";

import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useAccountStore} from "@/features/account/stores/account-store";

type WithdrawalStep =
    | "WITHDRAWAL"
    | "OTP"
    | "RESULT";

export default function WithdrawalPageContent() {
    const t = useTranslations("withdrawal");
    const tErrors = useTranslations("errors");

    const router = useRouter();

    const accounts = useAccountStore(
        (state) => state.accounts,
    );

    const accountsInitialized =
        useAccountStore(
            (state) => state.initialized,
        );

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
    ] = useState(false);

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
                currency: "VND",
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

    useEffect(() => {
        if (accountsInitialized) {
            return;
        }

        void getMyAccounts();
    }, [
        accountsInitialized,
        getMyAccounts,
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

    const availableAccounts =
        accounts.filter(
            (account) =>
                account.status ===
                AccountStatus.ACTIVE &&
                isAmountGreaterThanZero(
                    account.availableBalance,
                ),
        );

    useEffect(() => {
        if (
            !accountsInitialized ||
            selectedAccount ||
            availableAccounts.length <= 0
        ) {
            return;
        }

        const firstAccount =
            availableAccounts[0]

        if (firstAccount) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setSelectedAccount(firstAccount);

            form.setValue(
                "currency",
                firstAccount.currency,
            );
        }
    }, [accounts, accountsInitialized, selectedAccount, form, availableAccounts]);

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
                    accounts={availableAccounts}
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