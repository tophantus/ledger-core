"use client";

import {useCallback} from "react";

import {webhookApi} from "../api/webhook-api";
import type {
    WebhookFilters,
} from "../types/webhook";

export function useWebhooks() {
    const getWebhooks = useCallback(
        async (
            filters: WebhookFilters = {},
        ) => {
            return webhookApi.getAll(
                filters,
            );
        },
        [],
    );

    return {
        getWebhooks,
    };
}