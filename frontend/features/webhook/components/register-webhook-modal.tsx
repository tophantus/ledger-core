"use client";

import {Check, Copy, X} from "lucide-react";
import {useState} from "react";
import {Controller, useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import type {AccountSummary} from "@/features/account/types/account";

import {useRegisterWebhook} from "../hooks/use-register-webhook";
import {
    registerWebhookSchema,
    WEBHOOK_EVENT_TYPES,
    type RegisterWebhookForm,
} from "../schemas/register-webhook-schema";
import type {
    RegisterWebhookResponse,
} from "../types/webhook";

interface RegisterWebhookModalProps {
    open: boolean;
    accounts: AccountSummary[];
    onClose: () => void;
    onSuccess: () => void;
}

const DEFAULT_VALUES: RegisterWebhookForm = {
    accountId: "",
    url: "",
    eventTypes: [],
};

export function RegisterWebhookModal({
                                         open,
                                         accounts,
                                         onClose,
                                         onSuccess,
                                     }: RegisterWebhookModalProps) {
    const t = useTranslations("webhook");
    const tErrors = useTranslations("errors");

    const {registerWebhook} =
        useRegisterWebhook();

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    const [registeredWebhook, setRegisteredWebhook] =
        useState<RegisterWebhookResponse | null>(
            null,
        );

    const [isSecretCopied, setIsSecretCopied] =
        useState(false);

    const {
        register,
        control,
        handleSubmit,
        reset,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<RegisterWebhookForm>({
        resolver: zodResolver(
            registerWebhookSchema,
        ),
        defaultValues: DEFAULT_VALUES,
    });

    const handleClose = () => {
        if (registeredWebhook) {
            setRegisteredWebhook(null);
            setIsSecretCopied(false);
            reset(DEFAULT_VALUES);
            setErrorMessage(null);

            onSuccess();
            return;
        }

        reset(DEFAULT_VALUES);
        setErrorMessage(null);
        setRegisteredWebhook(null);
        setIsSecretCopied(false);

        onClose();
    };

    const handleCopySecret = async () => {
        if (!registeredWebhook) {
            return;
        }

        try {
            await navigator.clipboard.writeText(
                registeredWebhook.secret,
            );

            setIsSecretCopied(true);

            window.setTimeout(() => {
                setIsSecretCopied(false);
            }, 2000);
        } catch {
            setIsSecretCopied(false);
        }
    };

    const onSubmit = async (
        values: RegisterWebhookForm,
    ) => {
        setErrorMessage(null);

        try {
            const response =
                await registerWebhook({
                    accountId:
                    values.accountId,
                    url: values.url,
                    eventTypes:
                    values.eventTypes,
                });

            if (!response.success) {
                setErrorMessage(
                    response.code &&
                    tErrors.has(response.code)
                        ? tErrors(response.code)
                        : tErrors("fallback"),
                );

                return;
            }

            setRegisteredWebhook(
                response.data,
            );
            setIsSecretCopied(false);
        } catch {
            setErrorMessage(
                tErrors("fallback"),
            );
        }
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
                    if (!registeredWebhook) {
                        handleClose();
                    }
                }
            }}
        >
            <div
                className="
                    w-full
                    max-w-lg
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    shadow-lg
                "
            >
                {registeredWebhook ? (
                    <>
                        {/* Success header */}
                        <div
                            className="
                                flex
                                items-center
                                justify-between
                                border-b
                                border-border
                                px-6
                                py-4
                            "
                        >
                            <div>
                                <h2 className="
                                    text-lg
                                    font-semibold
                                    text-primary
                                ">
                                    {t(
                                        "register.success.title",
                                    )}
                                </h2>

                                <p className="
                                    mt-1
                                    text-sm
                                    text-muted
                                ">
                                    {t(
                                        "register.success.description",
                                    )}
                                </p>
                            </div>

                            <button
                                type="button"
                                onClick={handleClose}
                                className="
                                    rounded-md
                                    p-2
                                    text-muted
                                    transition
                                    hover:bg-secondary
                                    hover:text-secondary-foreground
                                "
                                aria-label={t(
                                    "register.close",
                                )}
                            >
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        {/* Success content */}
                        <div className="p-6">
                            <div className="
                                rounded-md
                                border
                                border-success/20
                                bg-success/10
                                px-4
                                py-3
                            ">
                                <div className="
                                    flex
                                    items-start
                                    gap-3
                                ">
                                    <div className="
                                        mt-0.5
                                        flex
                                        h-8
                                        w-8
                                        shrink-0
                                        items-center
                                        justify-center
                                        rounded-full
                                        bg-success/10
                                        text-success
                                    ">
                                        <Check className="h-4 w-4" />
                                    </div>

                                    <div>
                                        <p className="
                                            text-sm
                                            font-medium
                                            text-foreground
                                        ">
                                            {t(
                                                "register.success.registered",
                                            )}
                                        </p>

                                        <p className="
                                            mt-1
                                            text-sm
                                            text-muted
                                        ">
                                            {t(
                                                "register.success.secretWarning",
                                            )}
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="mt-6">
                                <label className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                ">
                                    {t(
                                        "register.success.secret",
                                    )}
                                </label>

                                <div className="
                                    flex
                                    items-center
                                    gap-2
                                ">
                                    <div className="
                                        min-w-0
                                        flex-1
                                        rounded-md
                                        border
                                        border-border
                                        bg-background
                                        px-3
                                        py-2.5
                                    ">
                                        <code className="
                                            block
                                            break-all
                                            text-sm
                                            text-foreground
                                        ">
                                            {
                                                registeredWebhook.secret
                                            }
                                        </code>
                                    </div>

                                    <Button
                                        type="button"
                                        variant="outline"
                                        onClick={
                                            handleCopySecret
                                        }
                                    >
                                        {isSecretCopied ? (
                                            <>
                                                <Check className="mr-2 h-4 w-4" />
                                                {t(
                                                    "register.success.copied",
                                                )}
                                            </>
                                        ) : (
                                            <>
                                                <Copy className="mr-2 h-4 w-4" />
                                                {t(
                                                    "register.success.copy",
                                                )}
                                            </>
                                        )}
                                    </Button>
                                </div>
                            </div>

                            <div className="
                                mt-6
                                rounded-md
                                border
                                border-warning/20
                                bg-warning/10
                                px-4
                                py-3
                                text-sm
                                text-foreground
                            ">
                                {t(
                                    "register.success.saveWarning",
                                )}
                            </div>

                            <div className="
                                mt-6
                                flex
                                justify-end
                            ">
                                <Button
                                    type="button"
                                    onClick={
                                        handleClose
                                    }
                                >
                                    {t(
                                        "register.success.done",
                                    )}
                                </Button>
                            </div>
                        </div>
                    </>
                ) : (
                    <>
                        {/* Register header */}
                        <div
                            className="
                                flex
                                items-center
                                justify-between
                                border-b
                                border-border
                                px-6
                                py-4
                            "
                        >
                            <div>
                                <h2 className="
                                    text-lg
                                    font-semibold
                                    text-primary
                                ">
                                    {t(
                                        "register.title",
                                    )}
                                </h2>

                                <p className="
                                    mt-1
                                    text-sm
                                    text-muted
                                ">
                                    {t(
                                        "register.description",
                                    )}
                                </p>
                            </div>

                            <button
                                type="button"
                                onClick={handleClose}
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
                                    "register.close",
                                )}
                            >
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        <form
                            onSubmit={handleSubmit(
                                onSubmit,
                            )}
                            className="p-6"
                        >
                            {errorMessage && (
                                <div
                                    className="
                                        mb-6
                                        rounded-md
                                        border
                                        border-danger/20
                                        bg-danger-subtle
                                        px-4
                                        py-3
                                        text-sm
                                        text-danger
                                    "
                                >
                                    {errorMessage}
                                </div>
                            )}

                            <div className="space-y-6">
                                {/* Account */}
                                <div>
                                    <label
                                        htmlFor="register-webhook-account"
                                        className="
                                            mb-2
                                            block
                                            text-sm
                                            font-medium
                                            text-foreground
                                        "
                                    >
                                        {t(
                                            "register.account",
                                        )}
                                    </label>

                                    <select
                                        id="register-webhook-account"
                                        {...register(
                                            "accountId",
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
                                            focus:ring-2
                                            focus:ring-primary/20
                                            disabled:cursor-not-allowed
                                            disabled:opacity-60
                                        "
                                    >
                                        <option value="">
                                            {t(
                                                "register.selectAccount",
                                            )}
                                        </option>

                                        {accounts.map(
                                            (
                                                account,
                                            ) => (
                                                <option
                                                    key={
                                                        account.id
                                                    }
                                                    value={
                                                        account.id
                                                    }
                                                >
                                                    {
                                                        account.accountNo
                                                    }{" "}
                                                    (
                                                    {
                                                        account.currency
                                                    }
                                                    )
                                                </option>
                                            ),
                                        )}
                                    </select>

                                    {errors.accountId && (
                                        <p className="
                                            mt-1
                                            text-xs
                                            text-danger
                                        ">
                                            {
                                                errors
                                                    .accountId
                                                    .message
                                            }
                                        </p>
                                    )}
                                </div>

                                {/* URL */}
                                <div>
                                    <label
                                        htmlFor="register-webhook-url"
                                        className="
                                            mb-2
                                            block
                                            text-sm
                                            font-medium
                                            text-foreground
                                        "
                                    >
                                        {t(
                                            "register.url",
                                        )}
                                    </label>

                                    <input
                                        id="register-webhook-url"
                                        type="url"
                                        placeholder={t(
                                            "register.urlPlaceholder",
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

                                {/* Event types */}
                                <Controller
                                    name="eventTypes"
                                    control={
                                        control
                                    }
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
                                                    "register.events",
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
                                        "register.cancel",
                                    )}
                                </Button>

                                <Button
                                    type="submit"
                                    loading={
                                        isSubmitting
                                    }
                                >
                                    {t(
                                        "register.submit",
                                    )}
                                </Button>
                            </div>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
}