"use client";

import {useCallback} from "react";

import {webhookApi} from "../api/webhook-api";

export function useWebhook() {
    const getWebhook = useCallback(
        async (webhookId: string) => {
            return webhookApi.getById(
                webhookId,
            );
        },
        [],
    );

    return {
        getWebhook,
    };
}