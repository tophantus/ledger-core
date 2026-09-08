"use client";

import {Button} from "@/components/ui/button";

import type {
    AccountHolder,
    AccountSummary,
} from "@/features/account/types/account";

import type {
    CreateTransferIntentResult,
} from "@/features/transaction/types/transaction";

import type {
    TransferOtpForm,
} from "@/features/transaction/schemas/transfer-schema";

import {formatMoney} from "@/lib/utils/currency";
import {useLocale, useTranslations} from "next-intl";
import {UseFormReturn} from "react-hook-form";

interface TransferConfirmationProps {
    intent: CreateTransferIntentResult;
    holder: AccountHolder;
    selectedAccount:
        | AccountSummary
        | null;
    otpForm: UseFormReturn<TransferOtpForm>;
    isConfirmLoading: boolean;
    onBack: () => void;
    onConfirm: () => void;
}

export function TransferConfirmation({
                                         intent,
                                         holder,
                                         selectedAccount,
                                         otpForm,
                                         isConfirmLoading,
                                         onBack,
                                         onConfirm,
                                     }: TransferConfirmationProps) {
    const t =
        useTranslations("transaction");

    const locale = useLocale();

    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onConfirm();
            }}
            className="
                rounded-lg
                border
                border-border
                bg-surface
                p-6
            "
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
                                "transfer.sourceAccount",
                            )}
                        </p>

                        <p className="mt-1 text-sm font-medium text-primary">
                            {
                                selectedAccount?.accountNo
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
                            {formatMoney(
                                intent.amount,
                                intent.currency,
                                locale,
                            )}
                        </p>
                    </div>
                </div>

                <div className="mt-4 border-t border-border pt-4">
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
                    className="
                        mb-2
                        block
                        text-sm
                        font-medium
                        text-foreground
                    "
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
                        text-foreground
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
                    onClick={onBack}
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
    );
}