import {useTranslations} from "next-intl";
import {AdminAccountDetail} from "@/features/admin/account/types/admin-account";

interface Props {
    user: AdminAccountDetail["user"];
}

export function AdminAccountOwnerInformation({
                                                 user,
                                             }: Props) {
    const t = useTranslations(
        "admin.account.detail",
    );

    return (
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
                {user.avatarUrl ? (
                    <img
                        src={user.avatarUrl}
                        alt={user.fullName}
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
                        {getInitial(user.fullName)}
                    </div>
                )}

                <div className="min-w-0 space-y-1">
                    <p className="font-medium text-primary">
                        {user.fullName}
                    </p>

                    <p className="break-all text-sm text-muted">
                        {user.email}
                    </p>

                    <p className="break-all font-mono text-xs text-muted">
                        {user.id}
                    </p>
                </div>
            </div>
        </section>
    );
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