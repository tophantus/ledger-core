"use client"

import {BottomBar} from "@/components/layout/bottom-bar";
import {LeftSidebar} from "@/components/layout/left-sidebar";
import {Topbar} from "@/components/layout/topbar";
import {ReactNode, useEffect} from "react";
import {useUserStore} from "@/features/user/stores/user-store";
import {hasAdminAccess} from "@/features/user/utils/user-role";
import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

export default function CustomerLayout({
                                           children,
                                       }: Readonly<{
    children: ReactNode;
}>) {
    const router = useRouter();
    const currentUser = useUserStore(
        (state) => state.currentUser,
    );

    const adminAccess =
        hasAdminAccess(currentUser);

    useEffect(() => {
        if (!currentUser) {
            return;
        }

        if (adminAccess) {
            router.replace(
                ROUTES.ADMIN.DASHBOARD,
            );
        }
    }, [
        currentUser,
        adminAccess,
        router,
    ]);

    if (!currentUser || adminAccess) {
        return null;
    }
    return (
        <div className="min-h-screen bg-background">
            <Topbar />

            <div className="flex">
                <LeftSidebar />

                <main
                    className="
                        min-w-0
                        flex-1
                        px-6
                        py-8
                        pb-24
                        md:pb-8
                    "
                >
                    <div className="mx-auto max-w-7xl">
                        {children}
                    </div>
                </main>
            </div>

            <BottomBar />
        </div>
    );
}