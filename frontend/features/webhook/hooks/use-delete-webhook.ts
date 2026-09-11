"use client";

import {webhookApi} from "../api/webhook-api";

export function useDeleteWebhook() {
    const removeWebhook = async (
        webhookId: string,
    ) => {
        return webhookApi.remove(
            webhookId,
        );
    };

    return {
        removeWebhook,
    };
}