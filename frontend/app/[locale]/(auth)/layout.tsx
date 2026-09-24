import type {ReactNode} from "react";
import {ProviderButton} from "@/features/auth/components/provider-button";

interface AuthLayoutProps {
    children: ReactNode;
}

export default function AuthLayout({
                                       children,
                                   }: AuthLayoutProps) {
    return (
        <main className="flex min-h-screen items-center justify-center bg-background px-4 py-8">
            <div className="w-full max-w-md">
                {children}
            </div>

            <ProviderButton />
        </main>
    );
}