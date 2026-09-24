"use client"

import type {ReactNode} from "react";

export default function ProviderLayout({
                                           children,
                                       }: Readonly<{
    children: ReactNode;
}>) {
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