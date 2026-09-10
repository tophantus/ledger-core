"use client";

import {useTranslations} from "next-intl";

import type {
    WithdrawalIntent,
} from "../../types/withdrawal";

import {WithdrawalListRow} from "./withdrawal-list-row";

interface WithdrawalListTableProps {
    withdrawals: WithdrawalIntent[];
}

export function WithdrawalListTable({
                                        withdrawals,
                                    }: WithdrawalListTableProps) {
    const t =
        useTranslations("withdrawal");

    return (
        <div className="
            overflow-hidden
            rounded-lg
            border
            border-border
            bg-surface
        ">
            <div className="overflow-x-auto">
                <table className="
                    w-full
                    min-w-[900px]
                    text-sm
                ">
                    <thead>
                    <tr className="
                            border-b
                            border-border
                            bg-background-subtle
                        ">
                        <th className="
                                whitespace-nowrap
                                px-4
                                py-3
                                text-left
                                text-xs
                                font-medium
                                text-muted
                            ">
                            {t(
                                "list.reference",
                            )}
                        </th>

                        <th className="
                                whitespace-nowrap
                                px-4
                                py-3
                                text-left
                                text-xs
                                font-medium
                                text-muted
                            ">
                            {t(
                                "list.account",
                            )}
                        </th>

                        <th className="
                                whitespace-nowrap
                                px-4
                                py-3
                                text-right
                                text-xs
                                font-medium
                                text-muted
                            ">
                            {t(
                                "list.amount",
                            )}
                        </th>

                        <th className="
                                whitespace-nowrap
                                px-4
                                py-3
                                text-left
                                text-xs
                                font-medium
                                text-muted
                            ">
                            {t(
                                "list.status",
                            )}
                        </th>

                        <th className="
                                whitespace-nowrap
                                px-4
                                py-3
                                text-left
                                text-xs
                                font-medium
                                text-muted
                            ">
                            {t(
                                "list.createdAt",
                            )}
                        </th>

                        <th className="
                                whitespace-nowrap
                                px-4
                                py-3
                                text-left
                                text-xs
                                font-medium
                                text-muted
                            ">
                            {t(
                                "list.completedAt",
                            )}
                        </th>
                    </tr>
                    </thead>

                    <tbody>
                    {withdrawals.map(
                        (withdrawal) => (
                            <WithdrawalListRow
                                key={
                                    withdrawal.id
                                }
                                withdrawal={
                                    withdrawal
                                }
                            />
                        ),
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}