"use client";

import {CheckCircle2} from "lucide-react";
import {
    useLocale,
    useTranslations,
} from "next-intl";
import {Button} from "@/components/ui/button";
import type {ConfirmWithdrawalRequestResponse} from "../types/withdrawal";
import {formatMoney} from "@/lib/utils/currency";

interface WithdrawalResultProps {
    result: ConfirmWithdrawalRequestResponse;
    onNewWithdrawal: () => void;
}

export function WithdrawalResult({
                                     result,
                                     onNewWithdrawal,
                                 }: WithdrawalResultProps) {
    const t =
        useTranslations("withdrawal");

    const locale = useLocale();

    return (
        <div className="
            rounded-lg
            border
            border-border
            bg-surface
            p-6
        ">
            <div className="text-center">
                <div className="
                    mx-auto
                    flex
                    h-12
                    w-12
                    items-center
                    justify-center
                    rounded-full
                    bg-success/10
                    text-success
                ">
                    <CheckCircle2 className="h-6 w-6" />
                </div>

                <h2 className="
                    mt-4
                    text-lg
                    font-semibold
                    text-primary
                ">
                    {t(
                        "successTitle",
                    )}
                </h2>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {t(
                        "successDescription",
                    )}
                </p>
            </div>

            <div className="
                mt-6
                rounded-lg
                bg-background-subtle
                p-4
            ">
                <div className="text-center">
                    <p className="text-xs text-muted">
                        {t(
                            "amount",
                        )}
                    </p>

                    <p className="
                        mt-1
                        text-2xl
                        font-semibold
                        tracking-tight
                        text-primary
                    ">
                        {formatMoney(
                            result.amount,
                            result.currency,
                            locale,
                        )}
                    </p>
                </div>

                <div className="
                    mt-5
                    border-t
                    border-border
                    pt-4
                ">
                    <p className="text-xs text-muted">
                        {t(
                            "reference",
                        )}
                    </p>

                    <div className="
                        mt-1
                        flex
                        items-center
                        justify-between
                        gap-3
                    ">
                        <p className="
                            break-all
                            font-mono
                            text-sm
                            font-medium
                            text-primary
                        ">
                            {
                                result.withdrawalReference
                            }
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

                    <p className="
                        mt-1
                        text-sm
                        font-medium
                        text-primary
                    ">
                        {new Date(
                            result.intentExpiresAt,
                        ).toLocaleString(
                            locale,
                        )}
                    </p>
                </div>
            </div>

            <div className="
                mt-4
                rounded-lg
                border
                border-primary/20
                bg-primary/5
                p-4
            ">
                <p className="
                    text-sm
                    font-medium
                    text-primary
                ">
                    {t(
                        "atmTitle",
                    )}
                </p>

                <p className="
                    mt-1
                    text-sm
                    leading-5
                    text-muted
                ">
                    {t(
                        "atmDescription",
                    )}
                </p>
            </div>

            <div className="mt-6">
                <Button
                    type="button"
                    className="w-full"
                    onClick={
                        onNewWithdrawal
                    }
                >
                    {t(
                        "newWithdrawal",
                    )}
                </Button>
            </div>
        </div>
    );
}