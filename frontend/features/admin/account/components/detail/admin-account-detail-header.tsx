"use client";

import {
    ArrowLeft,
    WalletCards,
} from "lucide-react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";
import {AdminAccountDetail} from "@/features/admin/account/types/admin-account";

interface AdminAccountDetailHeaderProps {
    account: AdminAccountDetail;
    onDeposit: () => void;
}

export function AdminAccountDetailHeader({
                                             account,
                                             onDeposit,
                                         }: AdminAccountDetailHeaderProps) {
    const t = useTranslations(
        "admin.account.detail",
    );

    const router = useRouter();

    return (
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
                    onClick={() =>
                        router.push(
                            ROUTES.ADMIN.ACCOUNTS,
                        )
                    }
                >
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    {t("back")}
                </Button>

                <div>
                    <h1 className="text-2xl font-semibold text-primary">
                        {t("title")}
                    </h1>
                </div>
            </div>

            <Button
                type="button"
                onClick={onDeposit}
            >
                <WalletCards className="mr-2 h-4 w-4" />
                {t("deposit")}
            </Button>
        </div>
    );
}