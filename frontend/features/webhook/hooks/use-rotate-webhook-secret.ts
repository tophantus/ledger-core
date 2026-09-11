"use client";

import {webhookApi} from "../api/webhook-api";

export function useRotateWebhookSecret() {
    const rotateWebhookSecret = async (
        webhookId: string,
    ) => {
        return webhookApi.rotateSecret(
            webhookId,
        );
    };

    return {
        rotateWebhookSecret,
    };
}