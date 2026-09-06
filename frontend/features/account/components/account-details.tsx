"use client";

import {ArrowLeft} from "lucide-react";
import {useTranslations} from "next-intl";

import {Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import type {Account} from "../types/account";

interface AccountDetailsProps {
    account: Account;
}

export function AccountDetails({
                                   account,
                               }: AccountDetailsProps) {
    const t = useTranslations("account");

    return (
        <section className="space-y-6">
            <Link
                href={ROUTES.DASHBOARD}
                className="inline-flex items-center gap-2 text-sm text-text-muted hover:text-text-primary"
            >
                <ArrowLeft className="h-4 w-4" />
                {t("details.back")}
            </Link>

            <div className="rounded-lg border border-border bg-surface">
                <div className="border-b border-border px-6 py-5">
                    <p className="text-sm text-text-muted">
                        {t("details.title")}
                    </p>

                    <h1 className="mt-1 text-xl font-semibold text-text-primary">
                        {account.accountNo}
                    </h1>
                </div>

                <div className="grid gap-6 p-6 sm:grid-cols-2 lg:grid-cols-3">
                    <div>
                        <p className="text-sm text-text-muted">
                            {t("details.balance")}
                        </p>

                        <p className="mt-1 text-2xl font-semibold text-text-primary">
                            {account.balance}{" "}
                            {account.currency}
                        </p>
                    </div>

                    <div>
                        <p className="text-sm text-text-muted">
                            {t("details.accountNumber")}
                        </p>

                        <p className="mt-1 font-medium text-text-primary">
                            {account.accountNo}
                        </p>
                    </div>

                    <div>
                        <p className="text-sm text-text-muted">
                            {t("details.currency")}
                        </p>

                        <p className="mt-1 font-medium text-text-primary">
                            {account.currency}
                        </p>
                    </div>

                    <div>
                        <p className="text-sm text-text-muted">
                            {t("details.status")}
                        </p>

                        <p className="mt-1 font-medium text-text-primary">
                            {t(
                                `statuses.${account.status}`,
                            )}
                        </p>
                    </div>

                    <div>
                        <p className="text-sm text-text-muted">
                            {t("details.createdAt")}
                        </p>

                        <p className="mt-1 font-medium text-text-primary">
                            {new Date(
                                account.createdAt,
                            ).toLocaleString()}
                        </p>
                    </div>

                    <div>
                        <p className="text-sm text-text-muted">
                            {t("details.updatedAt")}
                        </p>

                        <p className="mt-1 font-medium text-text-primary">
                            {new Date(
                                account.updatedAt,
                            ).toLocaleString()}
                        </p>
                    </div>
                </div>
            </div>
        </section>
    );
}