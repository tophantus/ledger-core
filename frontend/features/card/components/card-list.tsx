"use client";

import type {CardInfo} from "../types/card";

import {CardItem} from "./card-item";
import {CreateCardItem} from "./create-card-item";

interface CardListProps {
    cards: CardInfo[];
    onReveal: (card: CardInfo) => void;
    onCreate: () => void;
}

export function CardList({
                             cards,
                             onReveal,
                             onCreate,
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

            <CreateCardItem
                onClick={onCreate}
            />
        </div>
    );
}