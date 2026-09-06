"use client";

import {X} from "lucide-react";
import {useEffect} from "react";
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

    const {createAccount} =
        useCreateAccount();

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

    useEffect(() => {
        if (!open) {
            reset({
                currency: "VND",
            });
        }
    }, [open, reset]);

    if (!open) {
        return null;
    }

    const onSubmit = async (
        values: CreateAccountForm,
    ) => {
        try {
            const response =
                await createAccount({
                    currency: values.currency,
                });

            if (!response.success) {
                return;
            }

            reset({
                currency: "VND",
            });

            onSuccess();
        } catch {
        }
    };

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
                    onClose();
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
                        onClick={onClose}
                        className="
                            rounded-md
                            p-2
                            text-muted
                            transition
                            hover:bg-secondary
                            hover:text-secondary-foreground
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
                            onClick={onClose}
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