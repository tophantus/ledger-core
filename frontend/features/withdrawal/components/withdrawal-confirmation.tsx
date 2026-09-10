"use client";

import {UseFormReturn} from "react-hook-form";
import {
    useLocale,
    useTranslations,
} from "next-intl";

import {Button} from "@/components/ui/button";

import type {AccountSummary} from "@/features/account/types/account";
import type {WithdrawalRequestResponse} from "../types/withdrawal";
import type {WithdrawalOtpForm} from "../schemas/withdrawal-schema";

import {formatMoney} from "@/lib/utils/currency";

interface WithdrawalConfirmationProps {
    request: WithdrawalRequestResponse;
    selectedAccount:
        | AccountSummary
        | null;

    otpForm: UseFormReturn<WithdrawalOtpForm>;

    isConfirmLoading: boolean;

    onBack: () => void;
    onConfirm: () => void;
}

export function WithdrawalConfirmation({
                                           request,
                                           selectedAccount,
                                           otpForm,
                                           isConfirmLoading,
                                           onBack,
                                           onConfirm,
                                       }: WithdrawalConfirmationProps) {
    const t =
        useTranslations("withdrawal");

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
                        "confirmTitle",
                    )}
                </h2>

                <p className="mt-1 text-sm text-muted">
                    {t(
                        "confirmDescription",
                    )}
                </p>
            </div>

            <div className="
                mt-6
                rounded-lg
                bg-background-subtle
                p-4
            ">
                <div className="
                    flex
                    items-center
                    justify-between
                    gap-4
                ">
                    <div>
                        <p className="text-xs text-muted">
                            {t(
                                "sourceAccount",
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
                                "amount",
                            )}
                        </p>

                        <p className="mt-1 text-sm font-semibold text-primary">
                            {formatMoney(
                                request.amount,
                                request.currency,
                                locale,
                            )}
                        </p>
                    </div>
                </div>

                <div className="
                    mt-4
                    border-t
                    border-border
                    pt-4
                ">
                    <p className="text-xs text-muted">
                        {t(
                            "expiresAt",
                        )}
                    </p>

                    <p className="mt-1 text-sm font-medium text-primary">
                        {new Date(
                            request.expiresAt,
                        ).toLocaleString(
                            locale,
                        )}
                    </p>
                </div>
            </div>

            <div className="mt-6">
                <label
                    htmlFor="withdrawal-otp"
                    className="
                        mb-2
                        block
                        text-sm
                        font-medium
                        text-foreground
                    "
                >
                    {t(
                        "otp",
                    )}
                </label>

                <input
                    id="withdrawal-otp"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    {...otpForm.register(
                        "otp",
                    )}
                    disabled={
                        isConfirmLoading
                    }
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
                        disabled:cursor-not-allowed
                        disabled:opacity-60
                    "
                    placeholder={t(
                        "otpPlaceholder",
                    )}
                />

                {otpForm.formState.errors
                    .otp && (
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

            <div className="
                mt-6
                flex
                justify-between
                gap-3
            ">
                <Button
                    type="button"
                    variant="outline"
                    onClick={onBack}
                    disabled={
                        isConfirmLoading
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
                        "confirm",
                    )}
                </Button>
            </div>
        </form>
    );
}