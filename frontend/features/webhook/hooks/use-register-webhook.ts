"use client";

import {webhookApi} from "../api/webhook-api";
import type {
    RegisterWebhookRequest,
} from "../types/webhook";

export function useRegisterWebhook() {
    const registerWebhook = async (
        request: RegisterWebhookRequest,
    ) => {
        return webhookApi.register(request);
    };

    return {
        registerWebhook,
    };
}