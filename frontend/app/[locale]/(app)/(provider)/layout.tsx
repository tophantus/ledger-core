import type {ReactNode} from "react";

import {AuthButton} from "@/features/provider/components/auth-button";

export default function ProviderLayout({
                                           children,
                                       }: Readonly<{
    children: ReactNode;
}>) {
    return (
        <>
            {children}
            <AuthButton />
        </>
    );
}