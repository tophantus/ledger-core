"use client";

import {
    Pencil,
    XCircle,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {AccountSummary} from "@/features/account/types/account";
import {CreditRepaymentMandate} from "@/features/credit/types/credit-facility";

interface CreditRepaymentMandateConfiguredProps {
    mandate: CreditRepaymentMandate;
    accounts: AccountSummary[];
    onEdit: () => void;
    onRevoke: () => void;
    isRevoking: boolean;
}

export function CreditRepaymentMandateConfigured({
                                                     mandate,
                                                     accounts,
                                                     onEdit,
                                                     onRevoke,
                                                     isRevoking,
                                                 }: CreditRepaymentMandateConfiguredProps) {
    const t = useTranslations("credit");

    const account = accounts.find(
        (item) =>
            item.id === mandate.accountId,
    );

    return (
        <div className="mt-4">
            <div
                className="
                    grid
                    gap-4
                    sm:grid-cols-2
                "
            >
                <div>
                    <p className="text-xs text-text-muted">
                        {t(
                            "facility.repaymentMandate.account",
                        )}
                    </p>

                    <p className="mt-1 text-sm font-medium text-text-primary">
                        {account?.accountNo ??
                            mandate.accountId}
                    </p>
                </div>

                <div>
                    <p className="text-xs text-text-muted">
                        {t(
                            "facility.repaymentMandate.repaymentType",
                        )}
                    </p>

                    <p className="mt-1 text-sm font-medium text-text-primary">
                        {t(
                            `facility.repaymentMandate.types.${mandate.repaymentType}`,
                        )}
                    </p>
                </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
                <button
                    type="button"
                    onClick={onEdit}
                    className="
                        inline-flex
                        items-center
                        gap-2
                        rounded-lg
                        border
                        border-border
                        bg-surface
                        px-3
                        py-2
                        text-sm
                        font-medium
                        text-text-primary
                        transition
                        hover:bg-surface-subtle
                    "
                >
                    <Pencil className="h-4 w-4" />

                    {t(
                        "facility.repaymentMandate.actions.edit",
                    )}
                </button>

                <button
                    type="button"
                    onClick={onRevoke}
                    disabled={isRevoking}
                    className="
                        inline-flex
                        items-center
                        gap-2
                        rounded-lg
                        border
                        border-danger/20
                        bg-danger/5
                        px-3
                        py-2
                        text-sm
                        font-medium
                        text-danger
                        transition
                        hover:bg-danger/10
                        disabled:cursor-not-allowed
                        disabled:opacity-60
                    "
                >
                    <XCircle className="h-4 w-4" />

                    {isRevoking
                        ? t(
                            "facility.repaymentMandate.actions.revoking",
                        )
                        : t(
                            "facility.repaymentMandate.actions.revoke",
                        )}
                </button>
            </div>
        </div>
    );
}