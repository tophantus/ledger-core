"use client";

import {zodResolver} from "@hookform/resolvers/zod";
import {Eye, EyeOff, X} from "lucide-react";
import {useTranslations} from "next-intl";
import {useState} from "react";
import {useForm} from "react-hook-form";

import {Button} from "@/components/ui/button";

import {useRevealCardDetails} from "../hooks/use-reveal-card-details";
import {
    createCardDetailsSchema,
    type CardDetailsFormValues,
} from "../schemas/card-details-schema";
import type {
    CardInfo,
    RevealedCardDetails,
} from "../types/card";

import {CardDetailsItem} from "./card-details-item";

interface CardDetailsModalProps {
    card: CardInfo | null;
    open: boolean;
    onClose: () => void;
}

export function CardDetailsModal({
                                     card,
                                     open,
                                     onClose,
                                 }: CardDetailsModalProps) {
    const t = useTranslations("card");
    const tErrors = useTranslations("errors");

    const {revealCardDetails} =
        useRevealCardDetails();

    const [details, setDetails] =
        useState<RevealedCardDetails | null>(
            null,
        );

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    const [showPin, setShowPin] =
        useState(false);

    const schema =
        createCardDetailsSchema(
            t("validation.pinRequired"),
            t("validation.pinInvalid"),
        );

    const {
        register,
        handleSubmit,
        reset,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<CardDetailsFormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            pin: "",
        },
    });

    const handleClose = () => {
        reset();
        setDetails(null);
        setErrorMessage(null);
        setShowPin(false);
        onClose();
    };

    const handleReveal = async (
        values: CardDetailsFormValues,
    ) => {
        if (!card) {
            return;
        }

        setErrorMessage(null);

        try {
            const response =
                await revealCardDetails(
                    card.cardId,
                    values.pin,
                );

            if (!response.success) {
                setErrorMessage(
                    response.code &&
                    tErrors.has(response.code)
                        ? tErrors(response.code)
                        : tErrors("fallback"),
                );

                return;
            }

            setDetails(response.data);
        } catch {
            setErrorMessage(
                tErrors("fallback"),
            );
        }
    };

    if (!open || !card) {
        return null;
    }

    return (
        <div
            className="
                fixed
                inset-0
                z-50
                flex
                items-center
                justify-center
                bg-black/60
                p-4
                backdrop-blur-sm
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
                    relative
                    w-full
                    max-w-md
                    rounded-2xl
                    border
                    border-border
                    bg-surface
                    p-6
                    shadow-xl
                "
            >
                {/* Header */}
                <div className="flex items-start justify-between">
                    <div>
                        <h2 className="text-lg font-semibold text-text-primary">
                            {t("details.title")}
                        </h2>

                        {!details && (
                            <p className="mt-1 text-sm text-text-muted">
                                {t(
                                    "details.enterPin",
                                )}
                            </p>
                        )}
                    </div>

                    <button
                        type="button"
                        onClick={handleClose}
                        disabled={isSubmitting}
                        className="
                            rounded-full
                            p-2
                            text-text-muted
                            transition-colors
                            hover:bg-surface-subtle
                            hover:text-text-primary
                            disabled:cursor-not-allowed
                            disabled:opacity-50
                        "
                        aria-label={t(
                            "actions.close",
                        )}
                    >
                        <X className="size-5" />
                    </button>
                </div>

                {!details ? (
                    <form
                        onSubmit={handleSubmit(
                            handleReveal,
                        )}
                        className="mt-6 space-y-4"
                    >
                        {errorMessage && (
                            <div
                                className="
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
                                htmlFor="card-pin"
                                className="
                                    text-sm
                                    font-medium
                                    text-text-primary
                                "
                            >
                                {t("pin")}
                            </label>

                            <div className="relative mt-2">
                                <input
                                    id="card-pin"
                                    type={
                                        showPin
                                            ? "text"
                                            : "password"
                                    }
                                    inputMode="numeric"
                                    autoComplete="off"
                                    {...register(
                                        "pin",
                                    )}
                                    disabled={
                                        isSubmitting
                                    }
                                    className="
                                        w-full
                                        rounded-lg
                                        border
                                        border-border
                                        bg-background
                                        px-3
                                        py-2.5
                                        pr-10
                                        text-sm
                                        text-text-primary
                                        outline-none
                                        transition
                                        focus:ring-2
                                        focus:ring-primary/20
                                        disabled:cursor-not-allowed
                                        disabled:opacity-60
                                    "
                                    placeholder={t(
                                        "details.pinPlaceholder",
                                    )}
                                />

                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowPin(
                                            (value) =>
                                                !value,
                                        )
                                    }
                                    disabled={
                                        isSubmitting
                                    }
                                    className="
                                        absolute
                                        right-2
                                        top-1/2
                                        -translate-y-1/2
                                        rounded
                                        p-1.5
                                        text-text-muted
                                        hover:text-text-primary
                                        disabled:cursor-not-allowed
                                        disabled:opacity-50
                                    "
                                    aria-label={
                                        showPin
                                            ? t(
                                                "actions.hidePin",
                                            )
                                            : t(
                                                "actions.showPin",
                                            )
                                    }
                                >
                                    {showPin ? (
                                        <EyeOff className="size-4" />
                                    ) : (
                                        <Eye className="size-4" />
                                    )}
                                </button>
                            </div>

                            {errors.pin && (
                                <p className="mt-1.5 text-sm text-danger">
                                    {
                                        errors
                                            .pin
                                            .message
                                    }
                                </p>
                            )}
                        </div>

                        <Button
                            type="submit"
                            className="w-full"
                            loading={isSubmitting}
                        >
                            {t(
                                "actions.reveal",
                            )}
                        </Button>
                    </form>
                ) : (
                    <div className="mt-6">
                        <CardDetailsItem
                            card={card}
                            details={details}
                        />
                    </div>
                )}
            </div>
        </div>
    );
}