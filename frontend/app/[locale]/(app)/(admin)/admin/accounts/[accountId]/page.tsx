"use client";

import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    ArrowLeft,
    WalletCards,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import {AdminDepositModal} from "@/features/admin/account/components/admin-deposit-modal";
import {useAdminAccounts} from "@/features/admin/account/hooks/use-admin-accounts";
import type {AdminAccountDetail} from "@/features/admin/account/types/admin-account";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

interface AdminAccountDetailPageProps {
    params: Promise<{
        accountId: string;
    }>;
}

export default function AdminAccountDetailPage({
                                                   params,
                                               }: AdminAccountDetailPageProps) {
    const t = useTranslations(
        "admin.account.detail",
    );

    const router = useRouter();

    const {
        getAdminAccountDetail,
    } = useAdminAccounts();

    const [account, setAccount] =
        useState<AdminAccountDetail | null>(
            null,
        );

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState(false);

    const [depositModalOpen, setDepositModalOpen] =
        useState(false);

    const [accountId, setAccountId] =
        useState<string | null>(null);

    const loadAccount = useCallback(
        async (id: string) => {
            try {
                setLoading(true);
                setError(false);

                const response =
                    await getAdminAccountDetail(
                        id,
                    );

                if (!response.success) {
                    setError(true);
                    return;
                }

                setAccount(response.data);
            } catch {
                setError(true);
            } finally {
                setLoading(false);
            }
        },
        [getAdminAccountDetail],
    );

    useEffect(() => {
        let mounted = true;

        const load = async () => {
            const {accountId: id} =
                await params;

            if (!mounted) {
                return;
            }

            setAccountId(id);
            await loadAccount(id);
        };

        void load();

        return () => {
            mounted = false;
        };
    }, [params, loadAccount]);

    const handleBack = () => {
        router.push(
            ROUTES.ADMIN.ACCOUNTS,
        );
    };

    const handleDepositSuccess = async () => {
        if (!accountId) {
            return;
        }

        await loadAccount(accountId);
    };

    if (loading) {
        return (
            <div className="text-sm text-muted">
                {t("loading")}
            </div>
        );
    }

    if (error || !account) {
        return (
            <div className="space-y-4">
                <Button
                    type="button"
                    variant="outline"
                    onClick={handleBack}
                >
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    {t("back")}
                </Button>

                <div
                    className="
                        rounded-lg
                        border
                        border-border
                        bg-surface
                        p-6
                        text-sm
                        text-muted
                    "
                >
                    {t("loadError")}
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div
                className="
                    flex
                    flex-col
                    gap-4
                    sm:flex-row
                    sm:items-center
                    sm:justify-between
                "
            >
                <div className="flex items-center gap-4">
                    <Button
                        type="button"
                        variant="outline"
                        onClick={handleBack}
                    >
                        <ArrowLeft className="mr-2 h-4 w-4" />
                        {t("back")}
                    </Button>

                    <div>
                        <h1 className="text-2xl font-semibold text-primary">
                            {t("title")}
                        </h1>

                        <p className="mt-1 text-sm text-muted">
                            {account.accountNo}
                        </p>
                    </div>
                </div>

                <Button
                    type="button"
                    onClick={() =>
                        setDepositModalOpen(true)
                    }
                >
                    <WalletCards className="mr-2 h-4 w-4" />
                    {t("deposit")}
                </Button>
            </div>

            {/* Account information */}
            <section
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                "
            >
                <div className="border-b border-border px-6 py-4">
                    <h2 className="font-semibold text-primary">
                        {t("accountInformation")}
                    </h2>
                </div>

                <div
                    className="
                        grid
                        gap-6
                        p-6
                        sm:grid-cols-2
                        lg:grid-cols-3
                    "
                >
                    <DetailItem
                        label={t(
                            "fields.accountNo",
                        )}
                        value={
                            account.accountNo
                        }
                    />

                    <DetailItem
                        label={t(
                            "fields.currency",
                        )}
                        value={account.currency}
                    />

                    <DetailItem
                        label={t(
                            "fields.balance",
                        )}
                        value={`${account.balance} ${account.currency}`}
                    />

                    <DetailItem
                        label={t(
                            "fields.status",
                        )}
                        value={t(
                            `statuses.${account.status}`,
                        )}
                    />

                    <DetailItem
                        label={t(
                            "fields.ledgerAccountId",
                        )}
                        value={
                            account.ledgerAccountId
                        }
                    />

                    <DetailItem
                        label={t(
                            "fields.createdAt",
                        )}
                        value={formatDate(
                            account.createdAt,
                        )}
                    />

                    <DetailItem
                        label={t(
                            "fields.updatedAt",
                        )}
                        value={formatDate(
                            account.updatedAt,
                        )}
                    />
                </div>
            </section>

            {/* Owner information */}
            <section
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                "
            >
                <div className="border-b border-border px-6 py-4">
                    <h2 className="font-semibold text-primary">
                        {t("ownerInformation")}
                    </h2>
                </div>

                <div className="flex items-center gap-4 p-6">
                    {account.user.avatarUrl ? (
                        <img
                            src={
                                account.user
                                    .avatarUrl
                            }
                            alt={
                                account.user
                                    .fullName
                            }
                            className="
                                h-14
                                w-14
                                shrink-0
                                rounded-full
                                object-cover
                            "
                        />
                    ) : (
                        <div
                            className="
                                flex
                                h-14
                                w-14
                                shrink-0
                                items-center
                                justify-center
                                rounded-full
                                bg-background
                                text-lg
                                font-semibold
                                text-muted
                            "
                        >
                            {getInitial(
                                account.user
                                    .fullName,
                            )}
                        </div>
                    )}

                    <div className="min-w-0 space-y-1">
                        <p className="font-medium text-primary">
                            {
                                account.user
                                    .fullName
                            }
                        </p>

                        <p className="break-all text-sm text-muted">
                            {
                                account.user
                                    .email
                            }
                        </p>

                        <p className="break-all font-mono text-xs text-muted">
                            {account.user.id}
                        </p>
                    </div>
                </div>
            </section>

            {/* Deposit modal */}
            {depositModalOpen && (
                <AdminDepositModal
                    open
                    accountId={account.id}
                    accountNo={
                        account.accountNo
                    }
                    currency={
                        account.currency
                    }
                    onClose={() =>
                        setDepositModalOpen(
                            false,
                        )
                    }
                    onSuccess={
                        handleDepositSuccess
                    }
                />
            )}
        </div>
    );
}

interface DetailItemProps {
    label: string;
    value: string;
}

function DetailItem({
                        label,
                        value,
                    }: DetailItemProps) {
    return (
        <div className="min-w-0 space-y-1">
            <p className="text-sm text-muted">
                {label}
            </p>

            <p className="break-all text-sm font-medium text-primary">
                {value}
            </p>
        </div>
    );
}

function formatDate(
    value: string,
): string {
    return new Date(value).toLocaleString();
}

function getInitial(
    fullName: string,
): string {
    return (
        fullName
            .trim()
            .charAt(0)
            .toUpperCase() || "?"
    );
}