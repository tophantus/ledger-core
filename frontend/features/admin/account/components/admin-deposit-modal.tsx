"use client";

import {useState} from "react";
import {zodResolver} from "@hookform/resolvers/zod";
import {CheckCircle2, X} from "lucide-react";
import {useTranslations} from "next-intl";
import {Controller, useForm} from "react-hook-form";

import {Button} from "@/components/ui/button";
import {useAdminTransactions} from "@/features/admin/transaction/hooks/use-admin-transactions";
import {generateTransactionReference} from "@/lib/utils/reference";

import {
    adminDepositSchema,
    type AdminDepositFormValues,
} from "../schemas/admin-deposit-schema";
import { MoneyInput } from "@/components/ui/money-input";

interface AdminDepositModalProps {
    open: boolean;
    accountId: string;
    accountNo: string;
    currency: string;
    onClose: () => void;
    onSuccess: () => void | Promise<void>;
}

export function AdminDepositModal({
                                      open,
                                      accountId,
                                      accountNo,
                                      currency,
                                      onClose,
                                      onSuccess,
                                  }: AdminDepositModalProps) {
    const t = useTranslations(
        "admin.account.deposit",
    );

    const tErrors = useTranslations("errors");

    const {deposit} =
        useAdminTransactions();

    const [submitError, setSubmitError] =
        useState<string | null>(null);

    const [success, setSuccess] =
        useState(false);

    const {
        control,
        handleSubmit,
        reset,
        formState: {
            isSubmitting,
        },
    } = useForm<AdminDepositFormValues>({
        resolver: zodResolver(
            adminDepositSchema,
        ),
        defaultValues: {
            accountId,
            amount: "",
        },
    });

    if (!open) {
        return null;
    }

    const getErrorMessage = (
        code?: string,
    ): string => {
        if (
            code &&
            tErrors.has(code)
        ) {
            return tErrors(code);
        }

        return tErrors("fallback");
    };

    const handleClose = async () => {
        if (isSubmitting) {
            return;
        }

        if (success) {
            await onSuccess();
        }

        reset();
        setSubmitError(null);
        setSuccess(false);
        onClose();
    };

    const onSubmit = async (
        values: AdminDepositFormValues,
    ) => {
        try {
            setSubmitError(null);

            const response =
                await deposit({
                    destinationAccountId: values.accountId,
                    amount: values.amount,
                    currency,
                    reference:
                        generateTransactionReference(),
                });

            if (!response.success) {
                setSubmitError(
                    getErrorMessage(
                        response.code,
                    ),
                );

                return;
            }

            reset();
            setSuccess(true);
        } catch {
            setSubmitError(
                tErrors("fallback"),
            );
        }
    };

    return (
        <div
            className="
                fixed
                inset-0
                z-50
                flex
                items-center
                justify-center
                bg-black/50
                p-4
            "
            onMouseDown={(event) => {
                if (
                    event.target ===
                    event.currentTarget
                ) {
                    void handleClose();
                }
            }}
        >
            <div
                role="dialog"
                aria-modal="true"
                aria-labelledby="deposit-modal-title"
                className="
                    w-full
                    max-w-md
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    shadow-xl
                "
            >
                {success ? (
                    <div className="space-y-6 p-6">
                        <div
                            className="
                                flex
                                flex-col
                                items-center
                                gap-3
                                text-center
                            "
                        >
                            <CheckCircle2
                                className="
                                    h-12
                                    w-12
                                    text-success
                                "
                            />

                            <div className="space-y-1">
                                <h2
                                    id="deposit-modal-title"
                                    className="
                                        text-lg
                                        font-semibold
                                        text-primary
                                    "
                                >
                                    {t(
                                        "success.title",
                                    )}
                                </h2>

                                <p className="text-sm text-muted">
                                    {t(
                                        "success.description",
                                    )}
                                </p>
                            </div>
                        </div>

                        <div className="flex justify-end">
                            <Button
                                type="button"
                                onClick={
                                    handleClose
                                }
                            >
                                {t("close")}
                            </Button>
                        </div>
                    </div>
                ) : (
                    <>
                        <div
                            className="
                                flex
                                items-center
                                justify-between
                                border-b
                                border-border
                                px-6
                                py-4
                            "
                        >
                            <div>
                                <h2
                                    id="deposit-modal-title"
                                    className="
                                        font-semibold
                                        text-primary
                                    "
                                >
                                    {t("title")}
                                </h2>

                                <p className="mt-1 text-sm text-muted">
                                    {accountNo}
                                </p>
                            </div>

                            <button
                                type="button"
                                onClick={
                                    handleClose
                                }
                                disabled={
                                    isSubmitting
                                }
                                aria-label={t(
                                    "close",
                                )}
                                className="
                                    rounded-md
                                    p-1.5
                                    text-muted
                                    transition
                                    hover:bg-background
                                    hover:text-primary
                                    disabled:cursor-not-allowed
                                    disabled:opacity-50
                                "
                            >
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        <form
                            onSubmit={handleSubmit(
                                onSubmit,
                            )}
                            className="space-y-5 p-6"
                        >
                            <div className="space-y-2">
                                <label
                                    htmlFor="deposit-amount"
                                    className="
                                        text-sm
                                        font-medium
                                        text-primary
                                    "
                                >
                                    {t("amount")}
                                </label>

                                <Controller
                                    name="amount"
                                    control={control}
                                    render={({field, fieldState}) => (
                                        <MoneyInput
                                            id="deposit-amount"
                                            value={field.value}
                                            currency={currency}
                                            onChange={field.onChange}
                                            error={
                                                fieldState.error
                                                    ? t(
                                                        fieldState.error.message ??
                                                        "invalidAmount",
                                                    )
                                                    : undefined
                                            }
                                            placeholder={t(
                                                "amountPlaceholder",
                                            )}
                                            autoFocus
                                            disabled={isSubmitting}
                                        />
                                    )}
                                />
                            </div>

                            <div className="space-y-2">
                                <label
                                    htmlFor="deposit-currency"
                                    className="
                                        text-sm
                                        font-medium
                                        text-primary
                                    "
                                >
                                    {t("currency")}
                                </label>

                                <input
                                    id="deposit-currency"
                                    type="text"
                                    value={currency}
                                    readOnly
                                    tabIndex={-1}
                                    className="
                                        h-10
                                        w-full
                                        rounded-md
                                        border
                                        border-border
                                        bg-background
                                        px-3
                                        text-sm
                                        font-medium
                                        text-muted
                                    "
                                />
                            </div>

                            {submitError && (
                                <p
                                    role="alert"
                                    className="text-sm text-danger"
                                >
                                    {submitError}
                                </p>
                            )}

                            <div className="flex justify-end gap-3">
                                <Button
                                    type="button"
                                    variant="outline"
                                    onClick={
                                        handleClose
                                    }
                                    disabled={
                                        isSubmitting
                                    }
                                >
                                    {t("cancel")}
                                </Button>

                                <Button
                                    type="submit"
                                    loading={
                                        isSubmitting
                                    }
                                >
                                    {t("submit")}
                                </Button>
                            </div>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
}