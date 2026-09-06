"use client";

import {ReactNode, useEffect, useRef} from "react";

import {useCurrentUser} from "../hooks/use-current-user";

interface CurrentUserProviderProps {
    children: ReactNode;
}

export function CurrentUserProvider({
                                        children,
                                    }: CurrentUserProviderProps) {
    const {getCurrentUser} = useCurrentUser();

    const initialized = useRef(false);

    useEffect(() => {
        if (initialized.current) {
            return;
        }

        initialized.current = true;

        void getCurrentUser();
    }, [getCurrentUser]);

    return children;
}