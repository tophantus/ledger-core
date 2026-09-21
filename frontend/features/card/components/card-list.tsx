"use client";

import {useTranslations} from "next-intl";
import {useEffect, useState} from "react";

import {useGetCards} from "../hooks/use-get-cards";
import {useCardStore} from "../stores/card-store";

import {CardItem} from "./card-item";
import {CardItemSkeleton} from "./card-item-skeleton";

export function CardList() {
    const t = useTranslations("card");
    const tErrors = useTranslations("errors");

    const {getCards} = useGetCards();

    const cards = useCardStore(
        (state) => state.cards,
    );

    const setCards = useCardStore(
        (state) => state.setCards,
    );

    const [isLoading, setIsLoading] =
        useState(true);

    const [hasError, setHasError] =
        useState(false);

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    useEffect(() => {
        let mounted = true;

        const loadCards = async () => {
            try {
                const response =
                    await getCards(0, 20);

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setHasError(true);

                    setErrorMessage(
                        response.code &&
                        tErrors.has(
                            response.code,
                        )
                            ? tErrors(
                                response.code,
                            )
                            : tErrors(
                                "fallback",
                            ),
                    );

                    return;
                }

                setCards(
                    response.data.content,
                );
            } catch {
                if (!mounted) {
                    return;
                }

                setHasError(true);
                setErrorMessage(
                    tErrors("fallback"),
                );
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadCards();

        return () => {
            mounted = false;
        };
    }, [
        getCards,
        setCards,
        tErrors,
    ]);

    if (isLoading) {
        return (
            <div
                className="
                    grid
                    gap-5
                    sm:grid-cols-2
                    lg:grid-cols-3
                "
            >
                {Array.from({
                    length: 3,
                }).map((_, index) => (
                    <CardItemSkeleton
                        key={index}
                    />
                ))}
            </div>
        );
    }

    if (hasError) {
        return (
            <div
                className="
                    rounded-xl
                    border
                    border-border
                    bg-surface
                    p-6
                "
            >
                <p className="text-sm text-danger">
                    {errorMessage ??
                        tErrors(
                            "fallback",
                        )}
                </p>
            </div>
        );
    }

    return (
        <section className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-primary">
                    {t("title")}
                </h1>

                <p className="mt-1 text-sm text-text-muted">
                    {t("description")}
                </p>
            </div>

            {cards.length === 0 ? (
                <div
                    className="
                        rounded-xl
                        border
                        border-border
                        bg-surface
                        p-8
                        text-center
                    "
                >
                    <p className="text-sm text-text-muted">
                        {t("empty")}
                    </p>
                </div>
            ) : (
                <div
                    className="
                        grid
                        gap-5
                        sm:grid-cols-2
                        lg:grid-cols-3
                    "
                >
                    {cards.map((card) => (
                        <CardItem
                            key={card.cardId}
                            card={card}
                        />
                    ))}
                </div>
            )}
        </section>
    );
}