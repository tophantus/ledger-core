"use client";

import {X} from "lucide-react";
import {useEffect} from "react";
import {
    Controller,
    useForm,
} from "react-hook-form";
import {
    zodResolver,
} from "@hookform/resolvers/zod";
import {
    useTranslations,
} from "next-intl";

import {Button} from "@/components/ui/button";

import {useUpdateWebhookSubscriptions} from "../hooks/use-update-webhook-subscriptions";
import {
    updateWebhookSubscriptionsSchema,
    WEBHOOK_EVENT_TYPES,
    type UpdateWebhookSubscriptionsForm,
} from "../schemas/update-webhook-subscriptions-schema";
import type {
    Webhook,
} from "../types/webhook";

interface UpdateWebhookSubscriptionsModalProps {
    open: boolean;
    webhook: Webhook;
    onClose: () => void;
    onSuccess: () => void;
}

export function UpdateWebhookSubscriptionsModal({
                                                   open,
                                                   webhook,
                                                   onClose,
                                                   onSuccess,
                                               }: UpdateWebhookSubscriptionsModalProps) {
    const t =
        useTranslations("webhook");

    const tErrors =
        useTranslations("errors");

    const {
        updateWebhookSubscriptions,
    } =
        useUpdateWebhookSubscriptions();

    const {
        control,
        handleSubmit,
        reset,
        setError,
        formState: {
            errors,
            isSubmitting,
        },
    } =
        useForm<UpdateWebhookSubscriptionsForm>(
            {
                resolver: zodResolver(
                    updateWebhookSubscriptionsSchema,
                ),
                defaultValues: {
                    eventTypes:
                        webhook.eventTypes,
                },
            },
        );

    useEffect(() => {
        if (!open) {
            return;
        }

        reset({
            eventTypes:
                webhook.eventTypes,
        });
    }, [
        open,
        webhook.eventTypes,
        reset,
    ]);

    const onSubmit = async (
        values: UpdateWebhookSubscriptionsForm,
    ) => {
        const currentEventTypes =
            [...webhook.eventTypes].sort();

        const nextEventTypes =
            [...values.eventTypes].sort();

        const hasChanges =
            currentEventTypes.length !==
                nextEventTypes.length ||
            currentEventTypes.some(
                (
                    eventType,
                    index,
                ) =>
                    eventType !==
                    nextEventTypes[index],
            );

        /*
         * Do not call the API when
         * the selected events have not changed.
         */
        if (!hasChanges) {
            onClose();
            return;
        }

        try {
            const response =
                await updateWebhookSubscriptions(
                    webhook.id,
                    {
                        eventTypes:
                            values.eventTypes,
                    },
                );

            if (!response.success) {
                setError(
                    "eventTypes",
                    {
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
                    },
                );

                return;
            }

            onSuccess();
        } catch {
            setError(
                "eventTypes",
                {
                    type: "server",
                    message:
                        tErrors("fallback"),
                },
            );
        }
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        reset({
            eventTypes:
                webhook.eventTypes,
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
                        ">
                            {t(
                                "updateSubscriptions.title",
                            )}
                        </h2>

                        <p className="
                            mt-1
                            text-sm
                            text-muted
                        ">
                            {t(
                                "updateSubscriptions.description",
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
                            "updateSubscriptions.close",
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
                    <Controller
                        name="eventTypes"
                        control={control}
                        render={({
                                     field,
                                 }) => (
                            <div>
                                <label className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                ">
                                    {t(
                                        "updateSubscriptions.events",
                                    )}
                                </label>

                                <div className="
                                    space-y-3
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    p-4
                                ">
                                    {WEBHOOK_EVENT_TYPES.map(
                                        (
                                            eventType,
                                        ) => {
                                            const checked =
                                                field.value.includes(
                                                    eventType,
                                                );

                                            return (
                                                <label
                                                    key={
                                                        eventType
                                                    }
                                                    className="
                                                        flex
                                                        cursor-pointer
                                                        items-start
                                                        gap-3
                                                    "
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={
                                                            checked
                                                        }
                                                        disabled={
                                                            isSubmitting
                                                        }
                                                        onChange={() => {
                                                            const nextValue =
                                                                checked
                                                                    ? field.value.filter(
                                                                        (
                                                                            value,
                                                                        ) =>
                                                                            value !==
                                                                            eventType,
                                                                    )
                                                                    : [
                                                                        ...field.value,
                                                                        eventType,
                                                                    ];

                                                            field.onChange(
                                                                nextValue,
                                                            );
                                                        }}
                                                        className="
                                                            mt-0.5
                                                            h-4
                                                            w-4
                                                            rounded
                                                            border-border
                                                            text-primary
                                                            focus:ring-primary/20
                                                        "
                                                    />

                                                    <span className="
                                                        text-sm
                                                        text-foreground
                                                    ">
                                                        {t(
                                                            `eventTypes.${eventType}`,
                                                        )}
                                                    </span>
                                                </label>
                                            );
                                        },
                                    )}
                                </div>

                                {errors.eventTypes && (
                                    <p className="
                                        mt-1
                                        text-xs
                                        text-danger
                                    ">
                                        {
                                            errors
                                                .eventTypes
                                                .message
                                        }
                                    </p>
                                )}
                            </div>
                        )}
                    />

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
                                "updateSubscriptions.cancel",
                            )}
                        </Button>

                        <Button
                            type="submit"
                            loading={
                                isSubmitting
                            }
                        >
                            {t(
                                "updateSubscriptions.submit",
                            )}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
