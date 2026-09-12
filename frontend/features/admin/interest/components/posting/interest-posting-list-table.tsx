"use client";

import {useTranslations} from "next-intl";

import type {
    InterestPosting,
} from "../../types/admin-interest";

import {InterestPostingListRow} from "./interest-posting-list-row";

interface InterestPostingListTableProps {
    postings: InterestPosting[];
}

export function InterestPostingListTable({
                                             postings,
                                         }: InterestPostingListTableProps) {
    const t = useTranslations(
        "admin.interest.postings",
    );

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
                    text-left
                    text-sm
                ">
                    <thead className="
                        border-b
                        border-border
                        bg-background-subtle
                    ">
                    <tr>
                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.account",
                            )}
                        </th>

                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.run",
                            )}
                        </th>

                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.period",
                            )}
                        </th>

                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.interest",
                            )}
                        </th>

                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.transaction",
                            )}
                        </th>

                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.postedAt",
                            )}
                        </th>

                        <th className="
                                px-4
                                py-3
                                font-medium
                                text-muted
                            ">
                            {t(
                                "columns.createdAt",
                            )}
                        </th>
                    </tr>
                    </thead>

                    <tbody className="
                        divide-y
                        divide-border
                    ">
                    {postings.map(
                        (posting) => (
                            <InterestPostingListRow
                                key={
                                    posting.id
                                }
                                posting={
                                    posting
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