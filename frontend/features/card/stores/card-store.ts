"use client";

import {create} from "zustand";

import type {CardInfo} from "../types/card";

interface CardState {
    cards: CardInfo[];

    setCards: (cards: CardInfo[]) => void;
    clearCards: () => void;
}

export const useCardStore =
    create<CardState>((set) => ({
        cards: [],

        setCards: (cards) =>
            set({
                cards,
            }),

        clearCards: () =>
            set({
                cards: [],
            }),
    }));