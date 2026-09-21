"use client";

import type {CardInfo} from "../types/card";

import {CardItem} from "./card-item";

interface CardListProps {
    cards: CardInfo[];
    onReveal: (card: CardInfo) => void;
}

export function CardList({
                             cards,
                             onReveal,
                         }: CardListProps) {
    return (
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
                    onReveal={onReveal}
                />
            ))}
        </div>
    );
}