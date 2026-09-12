"use client";

import {useTranslations} from "next-intl";

import type {
    InterestAccrual,
} from "../../types/admin-interest";

import {InterestAccrualListRow} from "./interest-accrual-list-row";

interface InterestAccrualListTableProps {
    accruals: InterestAccrual[];
}

export function InterestAccrualListTable({
                                             accruals,
                                         }: InterestAccrualListTableProps) {
    const t = useTranslations(
        "admin.interest.accruals",
    );

    return (
        <div className="
            overflow-x-auto
            rounded-md
            border
            border-border
        ">
            <table className="
                w-full
                min-w-[1100px]
                text-left
                text-sm
            ">
                <thead className="
                    border-b
                    border-border
                    bg-background
                ">
                <tr>
                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.account")}
                    </th>

                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.currency")}
                    </th>

                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.businessDate")}
                    </th>

                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.principal")}
                    </th>

                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.rate")}
                    </th>

                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.interest")}
                    </th>

                    {/*<th className="*/}
                    {/*        px-4*/}
                    {/*        py-3*/}
                    {/*        font-medium*/}
                    {/*        text-muted*/}
                    {/*    ">*/}
                    {/*    {t("columns.run")}*/}
                    {/*</th>*/}

                    <th className="
                            px-4
                            py-3
                            font-medium
                            text-muted
                        ">
                        {t("columns.createdAt")}
                    </th>
                </tr>
                </thead>

                <tbody>
                {accruals.map((accrual) => (
                    <InterestAccrualListRow
                        key={accrual.id}
                        accrual={accrual}
                    />
                ))}
                </tbody>
            </table>
        </div>
    );
}