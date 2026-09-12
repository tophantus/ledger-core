"use client";

import type {
    InterestPosting,
} from "../../types/admin-interest";

interface InterestPostingListRowProps {
    posting: InterestPosting;
}

function formatDate(
    value: string,
): string {
    return new Intl.DateTimeFormat(
        undefined,
        {
            year: "numeric",
            month: "short",
            day: "numeric",
        },
    ).format(new Date(value));
}

function formatDateTime(
    value: string,
): string {
    return new Intl.DateTimeFormat(
        undefined,
        {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        },
    ).format(new Date(value));
}

export function InterestPostingListRow({
                                           posting,
                                       }: InterestPostingListRowProps) {
    return (
        <tr className="
            transition
            hover:bg-background-subtle
        ">
            <td className="
                whitespace-nowrap
                px-4
                py-3
                font-medium
                text-foreground
            ">
                {posting.accountId}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                font-mono
                text-xs
                text-muted
            ">
                {posting.runId}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-muted
            ">
                <div>
                    {formatDate(
                        posting.periodStart,
                    )}
                </div>

                <div className="
                    text-xs
                    text-muted
                ">
                    →{" "}
                    {formatDate(
                        posting.periodEnd,
                    )}
                </div>
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                font-medium
                text-foreground
            ">
                {posting.interestAmount}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                font-mono
                text-xs
                text-muted
            ">
                {posting.transactionId}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-muted
            ">
                {formatDateTime(
                    posting.postedAt,
                )}
            </td>

            <td className="
                whitespace-nowrap
                px-4
                py-3
                text-muted
            ">
                {formatDateTime(
                    posting.createdAt,
                )}
            </td>
        </tr>
    );
}