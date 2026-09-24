import type {ReactNode} from "react";
import {redirect} from "next/navigation";

import {isProviderConfigured} from "@/lib/utils/provider";
import {ROUTES} from "@/lib/constants/routes";

export default function ProviderRegisterLayout({
                                                   children,
                                               }: Readonly<{
    children: ReactNode;
}>) {
    if (isProviderConfigured()) {
        redirect(ROUTES.PROVIDER.DASHBOARD);
    }

    return (
        <main className="min-h-screen bg-background">
            <div
                className="
                    flex
                    min-h-screen
                    items-center
                    justify-center
                    px-6
                    py-8
                "
            >
                <div className="w-full max-w-2xl">
                    {children}
                </div>
            </div>
        </main>
    );
}