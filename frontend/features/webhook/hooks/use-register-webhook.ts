"use client";

import {webhookApi} from "../api/webhook-api";
import type {
    RegisterWebhookRequest,
} from "../types/webhook";

export function useRegisterWebhook() {
    const registerWebhook = async (
        accountId: string,
        request: RegisterWebhookRequest,
    ) => {
        return webhookApi.register(
            accountId,
            request,
        );
    };

    return {
        registerWebhook,
    };
}