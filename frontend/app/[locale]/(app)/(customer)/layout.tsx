import {BottomBar} from "@/components/layout/bottom-bar";
import {LeftSidebar} from "@/components/layout/left-sidebar";
import {Topbar} from "@/components/layout/topbar";
import {ReactNode} from "react";

export default function CustomerLayout({
                                           children,
                                       }: Readonly<{
    children: ReactNode;
}>) {
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