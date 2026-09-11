"use client";

import {webhookApi} from "../api/webhook-api";
import type {
    UpdateWebhookRequest,
} from "../types/webhook";

export function useUpdateWebhook() {
    const updateWebhook = async (
        webhookId: string,
        request: UpdateWebhookRequest,
    ) => {
        return webhookApi.update(
            webhookId,
            request,
        );
    };

    return {
        updateWebhook,
    };
}