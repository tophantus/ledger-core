"use client";

import {X} from "lucide-react";
import {useState} from "react";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

import {useCreateAccount} from "../hooks/use-create-account";
import {
    createAccountSchema,
    type CreateAccountForm,
} from "../schemas/create-account-schema";

interface CreateAccountModalProps {
    open: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

const SUPPORTED_CURRENCIES = [
    "VND",
    "USD",
] as const;

export function CreateAccountModal({
                                       open,
                                       onClose,
                                       onSuccess,
                                   }: CreateAccountModalProps) {
    const t = useTranslations("account");
    const tErrors = useTranslations("errors");

    const {createAccount} =
        useCreateAccount();

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    const {
        register,
        handleSubmit,
        reset,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<CreateAccountForm>({
        resolver: zodResolver(
            createAccountSchema,
        ),
        defaultValues: {
            currency: "VND",
        },
    });

    const handleClose = () => {
        reset({
            currency: "VND",
        });

        setErrorMessage(null);

        onClose();
    };

    const onSubmit = async (
        values: CreateAccountForm,
    ) => {
        setErrorMessage(null);

        try {
            const response =
                await createAccount({
                    currency: values.currency,
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

            reset({
                currency: "VND",
            });

            setErrorMessage(null);

            onSuccess();
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
                    handleClose();
                }
            }}
        >
            <div
                className="
                    w-full
                    max-w-md
                    rounded-lg
                    border
                    border-border
                    bg-surface
                    shadow-lg
                "
            >
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
                        <h2 className="text-lg font-semibold text-primary">
                            {t("create.title")}
                        </h2>

                        <p className="mt-1 text-sm text-muted">
                            {t("create.description")}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={handleClose}
                        disabled={isSubmitting}
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
                            "create.close",
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

                    <div>
                        <label
                            htmlFor="create-account-currency"
                            className="
                                mb-2
                                block
                                text-sm
                                font-medium
                                text-foreground
                            "
                        >
                            {t("create.currency")}
                        </label>

                        <select
                            id="create-account-currency"
                            {...register("currency")}
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
                            "
                        >
                            {SUPPORTED_CURRENCIES.map(
                                (currency) => (
                                    <option
                                        key={currency}
                                        value={currency}
                                    >
                                        {currency}
                                    </option>
                                ),
                            )}
                        </select>

                        {errors.currency && (
                            <p className="mt-1 text-xs text-danger">
                                {
                                    errors.currency
                                        .message
                                }
                            </p>
                        )}
                    </div>

                    <div className="mt-6 flex justify-end gap-3">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleClose}
                            disabled={isSubmitting}
                        >
                            {t("create.cancel")}
                        </Button>

                        <Button
                            type="submit"
                            loading={isSubmitting}
                        >
                            {t("create.submit")}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}