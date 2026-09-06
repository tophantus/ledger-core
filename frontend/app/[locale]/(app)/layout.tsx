import {ReactNode} from "react";

import {CurrentUserProvider} from "@/features/user/providers/current-user-provider";

export default function AppLayout({
                                      children,
                                  }: Readonly<{
    children: ReactNode;
}>) {
    return (
        <CurrentUserProvider>
            {children}
        </CurrentUserProvider>
    );
}