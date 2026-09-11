"use client";

import {webhookApi} from "../api/webhook-api";
import type {
    UpdateWebhookSubscriptionsRequest,
} from "../types/webhook";

export function useUpdateWebhookSubscriptions() {
    const updateWebhookSubscriptions =
        async (
            webhookId: string,
            request: UpdateWebhookSubscriptionsRequest,
        ) => {
            return webhookApi.updateSubscriptions(
                webhookId,
                request,
            );
        };

    return {
        updateWebhookSubscriptions,
    };
}