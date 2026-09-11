"use client";

import {useCallback} from "react";

import {webhookApi} from "../api/webhook-api";
import type {
    WebhookDeliveryFilters,
} from "../types/webhook";

export function useWebhookDeliveries() {
    const getWebhookDeliveries =
        useCallback(
            async (
                webhookId: string,
                filters: WebhookDeliveryFilters = {},
            ) => {
                return webhookApi.getDeliveries(
                    webhookId,
                    filters,
                );
            },
            [],
        );

    return {
        getWebhookDeliveries,
    };
}