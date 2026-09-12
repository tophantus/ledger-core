"use client";

import {useLocale} from "next-intl";

import type {
    InterestAccrual,
} from "../../types/admin-interest";

interface InterestAccrualListRowProps {
    accrual: InterestAccrual;
}

export function InterestAccrualListRow({
                                           accrual,
                                       }: InterestAccrualListRowProps) {
    const locale = useLocale();

    const formatDate = (
        value: string,
    ) => {
        return new Intl.DateTimeFormat(
            locale,
            {
                year: "numeric",
                month: "short",
                day: "numeric",
            },
        ).format(
            new Date(
                `${value}T00:00:00`,
            ),
        );
    };

    const formatDateTime = (
        value: string,
    ) => {
        return new Intl.DateTimeFormat(
            locale,
            {
                year: "numeric",
                month: "short",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
            },
        ).format(new Date(value));
    };

    return (
        <tr className="
            border-b
            border-border
            last:border-b-0
            hover:bg-background/50
        ">
            <td className="
                px-4
                py-3
                font-mono
                text-xs
                text-foreground
            ">
                {accrual.accountId}
            </td>

            <td className="
                px-4
                py-3
                font-medium
                text-foreground
            ">
                {accrual.currency}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-foreground
            ">
                {formatDate(
                    accrual.businessDate,
                )}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-foreground
            ">
                {accrual.principalAmount}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-foreground
            ">
                {accrual.interestRate}%
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                font-medium
                text-foreground
            ">
                {accrual.interestAmount}
            </td>

            {/*<td className="*/}
            {/*    px-4*/}
            {/*    py-3*/}
            {/*    font-mono*/}
            {/*    text-xs*/}
            {/*    text-muted*/}
            {/*">*/}
            {/*    {accrual.runId}*/}
            {/*</td>*/}

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-muted
            ">
                {formatDateTime(
                    accrual.createdAt,
                )}
            </td>
        </tr>
    );
}