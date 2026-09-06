"use client";

import {
    Lock,
    MoreVertical,
    Power,
    XCircle,
} from "lucide-react";
import {
    useEffect,
    useRef,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {useActivateAccount} from "../hooks/use-activate-account";
import {useBlockAccount} from "../hooks/use-block-account";
import {useCloseAccount} from "../hooks/use-close-account";
import {useAccountStore} from "../stores/account-store";

import {isZeroAmount} from "@/lib/utils/money";

interface AccountActionsProps {
    onClosed: () => void;
}

export function AccountActions({
                                   onClosed,
                               }: AccountActionsProps) {
    const t = useTranslations("account");

    const account = useAccountStore(
        (state) => state.currentAccount,
    );

    const {blockAccount} =
        useBlockAccount();

    const {activateAccount} =
        useActivateAccount();

    const {closeAccount} =
        useCloseAccount();

    const [isOpen, setIsOpen] =
        useState(false);

    const [isLoading, setIsLoading] =
        useState(false);

    const menuRef =
        useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!isOpen) {
            return;
        }

        const handleClickOutside = (
            event: MouseEvent,
        ) => {
            if (
                menuRef.current &&
                !menuRef.current.contains(
                    event.target as Node,
                )
            ) {
                setIsOpen(false);
            }
        };

        document.addEventListener(
            "mousedown",
            handleClickOutside,
        );

        return () => {
            document.removeEventListener(
                "mousedown",
                handleClickOutside,
            );
        };
    }, [isOpen]);

    if (!account) {
        return null;
    }

    const handleBlock = async () => {
        setIsLoading(true);

        try {
            const response =
                await blockAccount(account.id);

            if (response.success) {
                setIsOpen(false);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleActivate = async () => {
        setIsLoading(true);

        try {
            const response =
                await activateAccount(
                    account.id,
                );

            if (response.success) {
                setIsOpen(false);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleClose = async () => {
        if (
            !isZeroAmount(
                account.balance,
            )
        ) {
            return;
        }

        setIsLoading(true);

        try {
            const response =
                await closeAccount(
                    account.id,
                );

            if (response.success) {
                setIsOpen(false);
                onClosed();
            }
        } finally {
            setIsLoading(false);
        }
    };

    const canBlock =
        account.status === "ACTIVE";

    const canActivate =
        account.status === "BLOCKED";

    const canClose =
        account.status !== "CLOSED" &&
        isZeroAmount(account.balance);

    const hasActions =
        canBlock ||
        canActivate ||
        canClose;

    if (!hasActions) {
        return null;
    }

    return (
        <div
            ref={menuRef}
            className="relative"
        >
            <button
                type="button"
                onClick={() =>
                    setIsOpen(
                        (open) => !open,
                    )
                }
                disabled={isLoading}
                className="
                    rounded-md
                    p-2
                    text-muted
                    transition
                    hover:bg-background-subtle
                    hover:text-primary
                    disabled:cursor-not-allowed
                    disabled:opacity-50
                "
                aria-label={t(
                    "details.actions.open",
                )}
                aria-expanded={isOpen}
            >
                <MoreVertical className="h-5 w-5" />
            </button>

            {isOpen && (
                <div
                    className="
                        absolute
                        right-0
                        top-full
                        z-20
                        mt-2
                        w-48
                        rounded-lg
                        border
                        border-border
                        bg-surface
                        p-1
                        shadow-lg
                    "
                >
                    {canBlock && (
                        <button
                            type="button"
                            onClick={handleBlock}
                            disabled={isLoading}
                            className="
                                flex
                                w-full
                                items-center
                                gap-3
                                rounded-md
                                px-3
                                py-2
                                text-sm
                                text-primary
                                transition
                                hover:bg-background-subtle
                                disabled:opacity-50
                            "
                        >
                            <Lock className="h-4 w-4" />
                            {t(
                                "details.actions.block",
                            )}
                        </button>
                    )}

                    {canActivate && (
                        <button
                            type="button"
                            onClick={handleActivate}
                            disabled={isLoading}
                            className="
                                flex
                                w-full
                                items-center
                                gap-3
                                rounded-md
                                px-3
                                py-2
                                text-sm
                                text-primary
                                transition
                                hover:bg-background-subtle
                                disabled:opacity-50
                            "
                        >
                            <Power className="h-4 w-4" />
                            {t(
                                "details.actions.activate",
                            )}
                        </button>
                    )}

                    {canClose && (
                        <button
                            type="button"
                            onClick={handleClose}
                            disabled={isLoading}
                            className="
                                flex
                                w-full
                                items-center
                                gap-3
                                rounded-md
                                px-3
                                py-2
                                text-sm
                                text-danger
                                transition
                                hover:bg-background-subtle
                                disabled:opacity-50
                            "
                        >
                            <XCircle className="h-4 w-4" />
                            {t(
                                "details.actions.close",
                            )}
                        </button>
                    )}
                </div>
            )}
        </div>
    );
}