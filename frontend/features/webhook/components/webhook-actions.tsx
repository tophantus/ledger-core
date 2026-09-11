"use client";

import {
    MoreHorizontal,
    Pencil,
    RotateCw,
    Settings2,
    Trash2,
} from "lucide-react";
import {
    useTranslations,
} from "next-intl";
import React, {
    useEffect,
    useRef,
    useState,
} from "react";

import {Button} from "@/components/ui/button";
import {useDeleteWebhook} from "@/features/webhook/hooks/use-delete-webhook";
import {
    useRotateWebhookSecret,
} from "@/features/webhook/hooks/use-rotate-webhook-secret";
import {UpdateWebhookModal} from "@/features/webhook/components/update-webhook-modal";
import {
    UpdateWebhookSubscriptionsModal,
} from "@/features/webhook/components/update-webhook-subscriptions-modal";
import type {
    Webhook,
} from "@/features/webhook/types/webhook";
import {RotateWebhookSecretResult} from "@/features/webhook/components/rotate-webhook-secret-result";

interface WebhookActionsProps {
    webhook: Webhook;
    onRemoved: () => void;
    onUpdated: () => void;
}

interface ActionMenuPosition {
    top: number;
    right: number;
}

export function WebhookActions({
                                   webhook,
                                   onRemoved,
                                   onUpdated,
                               }: WebhookActionsProps) {
    const t =
        useTranslations("webhook");

    const {
        removeWebhook,
    } = useDeleteWebhook();

    const {
        rotateWebhookSecret,
    } = useRotateWebhookSecret();

    const [
        isOpen,
        setIsOpen,
    ] = useState(false);

    const [
        isRemoving,
        setIsRemoving,
    ] = useState(false);

    const [
        isRotating,
        setIsRotating,
    ] = useState(false);

    const [
        isUpdateModalOpen,
        setIsUpdateModalOpen,
    ] = useState(false);

    const [
        isSubscriptionsModalOpen,
        setIsSubscriptionsModalOpen,
    ] = useState(false);

    const [
        rotatedSecret,
        setRotatedSecret,
    ] = useState<string | null>(null);

    const [
        menuPosition,
        setMenuPosition,
    ] = useState<ActionMenuPosition | null>(
        null,
    );

    const actionsRef =
        useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!isOpen) {
            return;
        }

        const handleClickOutside = (
            event: MouseEvent,
        ) => {
            if (
                actionsRef.current &&
                !actionsRef.current.contains(
                    event.target as Node,
                )
            ) {
                setIsOpen(false);
                setMenuPosition(null);
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

    const handleToggleMenu = (
        event: React.MouseEvent<HTMLButtonElement>,
    ) => {
        if (isOpen) {
            setIsOpen(false);
            setMenuPosition(null);
            return;
        }

        const rect =
            event.currentTarget.getBoundingClientRect();

        setMenuPosition({
            top: rect.bottom + 8,
            right:
                window.innerWidth -
                rect.right,
        });

        setIsOpen(true);
    };

    const handleRemove = async () => {
        if (
            isRemoving ||
            webhook.status !== "ACTIVE"
        ) {
            return;
        }

        setIsRemoving(true);

        try {
            const response =
                await removeWebhook(
                    webhook.id,
                );

            if (response.success) {
                setIsOpen(false);
                setMenuPosition(null);
                onRemoved();
            }
        } finally {
            setIsRemoving(false);
        }
    };

    const handleRotateSecret = async () => {
        if (
            isRotating ||
            webhook.status !== "ACTIVE"
        ) {
            return;
        }

        setIsRotating(true);

        try {
            const response =
                await rotateWebhookSecret(
                    webhook.id,
                );

            if (response.success) {
                setIsOpen(false);
                setMenuPosition(null);
                setRotatedSecret(
                    response.data.secret,
                );
            }
        } finally {
            setIsRotating(false);
        }
    };

    const handleCloseSecret = () => {
        setRotatedSecret(null);
        onUpdated();
    };

    const handleUpdateSuccess = () => {
        setIsUpdateModalOpen(false);
        setIsOpen(false);
        setMenuPosition(null);
        onUpdated();
    };

    const handleSubscriptionsSuccess = () => {
        setIsSubscriptionsModalOpen(false);
        setIsOpen(false);
        setMenuPosition(null);
        onUpdated();
    };

    const actions = [
        {
            key: "update",
            icon: Pencil,
            onClick: () => {
                setIsOpen(false);
                setMenuPosition(null);
                setIsUpdateModalOpen(true);
            },
        },
        {
            key: "updateSubscriptions",
            icon: Settings2,
            onClick: () => {
                setIsOpen(false);
                setMenuPosition(null);
                setIsSubscriptionsModalOpen(
                    true,
                );
            },
        },
        {
            key: "rotateSecret",
            icon: RotateCw,
            onClick: handleRotateSecret,
        },
        {
            key: "remove",
            icon: Trash2,
            destructive: true,
            onClick: handleRemove,
        },
    ];

    return (
        <>
            <div
                ref={actionsRef}
                className="
                    relative
                    inline-flex
                "
            >
                <Button
                    type="button"
                    variant="ghost"
                    aria-label={t(
                        "actions.title",
                    )}
                    onClick={
                        handleToggleMenu
                    }
                >
                    <MoreHorizontal
                        className="
                            h-4
                            w-4
                        "
                    />
                </Button>

                {isOpen &&
                    menuPosition &&
                    webhook.status ===
                    "ACTIVE" && (
                        <div
                            className="
                                fixed
                                z-50
                                min-w-[220px]
                                overflow-hidden
                                rounded-lg
                                border
                                border-border
                                bg-background
                                p-1
                                shadow-lg
                            "
                            style={{
                                top: menuPosition.top,
                                right: menuPosition.right,
                            }}
                        >
                            {actions.map(
                                (
                                    action,
                                    index,
                                ) => {
                                    const Icon =
                                        action.icon;

                                    return (
                                        <div
                                            key={
                                                action.key
                                            }
                                        >
                                            {index ===
                                                3 && (
                                                    <div className="
                                                        my-1
                                                        border-t
                                                        border-border
                                                    " />
                                                )}

                                            <button
                                                type="button"
                                                disabled={
                                                    (action.key ===
                                                        "remove" &&
                                                        isRemoving) ||
                                                    (action.key ===
                                                        "rotateSecret" &&
                                                        isRotating)
                                                }
                                                className={`
flex
w-full
items-center
gap-2
rounded-md
px-3
py-2
text-left
text-sm
${
    action.destructive
        ? `
                                                            text-danger
                                                            hover:bg-danger/10
                                                        `
        : `
                                                            text-foreground
                                                            hover:bg-background-subtle
                                                        `
}
disabled:cursor-not-allowed
disabled:opacity-50
    `}
                                                onClick={
                                                    action.onClick
                                                }
                                            >
                                                <Icon
                                                    className="
                                                        h-4
                                                        w-4
                                                    "
                                                />

                                                {t(
                                                    `actions.${action.key}`,
                                                )}
                                            </button>
                                        </div>
                                    );
                                },
                            )}
                        </div>
                    )}
            </div>

            <UpdateWebhookModal
                open={
                    isUpdateModalOpen
                }
                webhook={webhook}
                onClose={() =>
                    setIsUpdateModalOpen(
                        false,
                    )
                }
                onSuccess={
                    handleUpdateSuccess
                }
            />

            <UpdateWebhookSubscriptionsModal
                open={
                    isSubscriptionsModalOpen
                }
                webhook={webhook}
                onClose={() =>
                    setIsSubscriptionsModalOpen(
                        false,
                    )
                }
                onSuccess={
                    handleSubscriptionsSuccess
                }
            />

            {rotatedSecret && (
                <RotateWebhookSecretResult
                    secret={rotatedSecret}
                    onClose={handleCloseSecret}
                />
            )}
        </>
    );
}