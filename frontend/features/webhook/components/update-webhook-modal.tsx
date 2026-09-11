"use client";

import {X} from "lucide-react";
import {useEffect} from "react";
import {
    useForm,
} from "react-hook-form";
import {
    zodResolver,
} from "@hookform/resolvers/zod";
import {
    useTranslations,
} from "next-intl";

import {Button} from "@/components/ui/button";

import {useUpdateWebhook} from "../hooks/use-update-webhook";
import {
    updateWebhookSchema,
    type UpdateWebhookForm,
} from "../schemas/update-webhook-schema";
import type {
    Webhook,
} from "../types/webhook";

interface UpdateWebhookModalProps {
    open: boolean;
    webhook: Webhook;
    onClose: () => void;
    onSuccess: () => void;
}

export function UpdateWebhookModal({
                                       open,
                                       webhook,
                                       onClose,
                                       onSuccess,
                                   }: UpdateWebhookModalProps) {
    const t =
        useTranslations("webhook");

    const tErrors =
        useTranslations("errors");

    const {
        updateWebhook,
    } = useUpdateWebhook();

    const {
        register,
        handleSubmit,
        reset,
        setError,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<UpdateWebhookForm>({
        resolver: zodResolver(
            updateWebhookSchema,
        ),
        defaultValues: {
            url: webhook.url,
        },
    });

    useEffect(() => {
        if (!open) {
            return;
        }

        reset({
            url: webhook.url,
        });
    }, [
        open,
        webhook.url,
        reset,
    ]);

    const onSubmit = async (
        values: UpdateWebhookForm,
    ) => {
        const trimmedUrl =
            values.url.trim();

        /*
         * Do not call the API when
         * the URL has not changed.
         */
        if (
            trimmedUrl ===
            webhook.url
        ) {
            onClose();
            return;
        }

        try {
            const response =
                await updateWebhook(
                    webhook.id,
                    {
                        url: trimmedUrl,
                    },
                );

            if (!response.success) {
                setError("url", {
                    type: "server",
                    message:
                        response.code &&
                        tErrors.has(
                            response.code,
                        )
                            ? tErrors(
                                response.code,
                            )
                            : response.message,
                });

                return;
            }

            onSuccess();
        } catch {
            setError("url", {
                type: "server",
                message:
                    tErrors("fallback"),
            });
        }
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        reset({
            url: webhook.url,
        });

        onClose();
    };

    if (!open) {
        return null;
    }

    return (
        <div
            className="
                fixed
                inset-0
                z-[60]
                flex
                items-center
                justify-center
                bg-black/40
                p-4
            "
            onMouseDown={(event) => {
                if (
                    event.target ===
                    event.currentTarget
                ) {
                    handleClose();
                }
            }}
        >
            <div className="
                w-full
                max-w-lg
                rounded-lg
                border
                border-border
                bg-surface
                shadow-lg
            ">
                <div className="
                    flex
                    items-center
                    justify-between
                    border-b
                    border-border
                    px-6
                    py-4
                ">
                    <div>
                        <h2 className="
                            text-lg
                            font-semibold
                            text-primary
                            text-center
                        ">
                            {t(
                                "update.title",
                            )}
                        </h2>

                        <p className="
                            mt-1
                            text-sm
                            text-muted
                        ">
                            {t(
                                "update.description",
                            )}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={
                            handleClose
                        }
                        disabled={
                            isSubmitting
                        }
                        className="
                            rounded-md
                            p-2
                            text-muted
                            transition
                            hover:bg-secondary
                            hover:text-secondary-foreground
                            disabled:cursor-not-allowed
                            disabled:opacity-50
                        "
                        aria-label={t(
                            "update.close",
                        )}
                    >
                        <X className="
                            h-5
                            w-5
                        " />
                    </button>
                </div>

                <form
                    onSubmit={handleSubmit(
                        onSubmit,
                    )}
                    className="p-6"
                >
                    <div className="
                        space-y-6
                    ">
                        <div>
                            <label
                                htmlFor="update-webhook-url"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    text-left
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "update.url",
                                )}
                            </label>

                            <input
                                id="update-webhook-url"
                                type="url"
                                placeholder={t(
                                    "update.urlPlaceholder",
                                )}
                                {...register(
                                    "url",
                                )}
                                disabled={
                                    isSubmitting
                                }
                                className="
                                    w-full
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2.5
                                    text-sm
                                    text-foreground
                                    outline-none
                                    transition
                                    placeholder:text-muted
                                    focus:ring-2
                                    focus:ring-primary/20
                                    disabled:cursor-not-allowed
                                    disabled:opacity-60
                                "
                            />

                            {errors.url && (
                                <p className="
                                    mt-1
                                    text-xs
                                    text-danger
                                ">
                                    {
                                        errors
                                            .url
                                            .message
                                    }
                                </p>
                            )}
                        </div>
                    </div>

                    <div className="
                        mt-6
                        flex
                        justify-end
                        gap-3
                    ">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={
                                handleClose
                            }
                            disabled={
                                isSubmitting
                            }
                        >
                            {t(
                                "update.cancel",
                            )}
                        </Button>

                        <Button
                            type="submit"
                            loading={
                                isSubmitting
                            }
                        >
                            {t(
                                "update.submit",
                            )}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
