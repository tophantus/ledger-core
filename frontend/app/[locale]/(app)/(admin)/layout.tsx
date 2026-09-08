"use client"

import {AdminLeftSidebar} from "@/components/layout/admin-left-sidebar";
import {Topbar} from "@/components/layout/topbar";
import { useRouter } from "@/i18n/routing";
import {ReactNode, useEffect} from "react";
import {useUserStore} from "@/features/user/stores/user-store";
import { hasAdminAccess } from "@/features/user/utils/user-role";
import {ROUTES} from "@/lib/constants/routes";
import {useProduct} from "@/features/product/hooks/use-product";

export default function AdminLayout({
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

    const {
        initialized,
        getActiveProducts,
    } = useProduct();

    useEffect(() => {
        if (
            !currentUser ||
            !adminAccess ||
            initialized
        ) {
            return;
        }

        void getActiveProducts();
    }, [
        currentUser,
        adminAccess,
        initialized,
        getActiveProducts,
    ]);

    useEffect(() => {
        if (!currentUser) {
            return;
        }

        if (!adminAccess) {
            router.replace(
                ROUTES.DASHBOARD,
            );
        }
    }, [
        currentUser,
        adminAccess,
        router,
    ]);

    if (!currentUser || !adminAccess) {
        return null;
    }
    return (
        <div className="min-h-screen bg-background">
            <Topbar />

            <div className="flex">
                <AdminLeftSidebar />

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
        </div>
    );
}