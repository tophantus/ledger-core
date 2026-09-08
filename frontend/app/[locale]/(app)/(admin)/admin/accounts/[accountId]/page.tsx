"use client";

import AdminAccountDetailPageContent from "@/features/admin/account/components/detail/admin-account-detail-page";

interface AdminAccountDetailPageProps {
    params: Promise<{
        accountId: string;
    }>;
}

export default function AdminAccountDetailPage({
                                                   params,
                                               }: AdminAccountDetailPageProps) {
    return (
        <AdminAccountDetailPageContent params={params}/>
    )
}
