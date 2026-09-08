"use client";

import {
    useCallback,
    useEffect,
    useState,
} from "react";


import {AdminAccountDetailHeader} from "./admin-account-detail-header";
import {AdminAccountInformation} from "./admin-account-information";
import {AdminAccountOwnerInformation} from "./admin-account-owner-information";
import {useAdminAccounts} from "@/features/admin/account/hooks/use-admin-accounts";
import {AdminAccountDetail} from "@/features/admin/account/types/admin-account";
import {AdminAccountDetailError} from "@/features/admin/account/components/detail/admin-account-detail-error";
import {AdminDepositModal} from "@/features/admin/account/components/admin-deposit-modal";
import {AdminAccountDetailSkeleton} from "@/features/admin/account/components/detail/admin-account-detail-skeleton";

interface AdminAccountDetailPageProps {
    params: Promise<{
        accountId: string;
    }>;
}

export default function AdminAccountDetailPageContent({
                                                   params,
                                               }: AdminAccountDetailPageProps) {
    const {
        getAdminAccountDetail,
    } = useAdminAccounts();

    const [account, setAccount] =
        useState<AdminAccountDetail | null>(null);

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
                    await getAdminAccountDetail(id);

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

    const handleDepositSuccess = async () => {
        if (!accountId) {
            return;
        }

        await loadAccount(accountId);
    };

    if (loading) {
        return <AdminAccountDetailSkeleton />;
    }

    if (error || !account) {
        return <AdminAccountDetailError />;
    }

    return (
        <div className="space-y-6">
            <AdminAccountDetailHeader
                account={account}
                onDeposit={() =>
                    setDepositModalOpen(true)
                }
            />

            <AdminAccountInformation
                account={account}
            />

            <AdminAccountOwnerInformation
                user={account.user}
            />

            {depositModalOpen && (
                <AdminDepositModal
                    open
                    accountId={account.id}
                    accountNo={account.accountNo}
                    currency={account.currency}
                    onClose={() =>
                        setDepositModalOpen(false)
                    }
                    onSuccess={handleDepositSuccess}
                />
            )}
        </div>
    );
}