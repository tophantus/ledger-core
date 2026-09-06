import {Topbar} from "@/components/layout/topbar";
import {CurrentUserProvider} from "@/features/user/providers/current-user-provider";
import {ReactNode} from "react";

export default function DashboardLayout({
                                            children,
                                        }: Readonly<{
    children: ReactNode;
}>) {
    return (
        <CurrentUserProvider>
            <div className="min-h-screen bg-background">
                <Topbar />

                <main className="mx-auto max-w-7xl px-6 py-8">
                    {children}
                </main>
            </div>
        </CurrentUserProvider>
    );
}