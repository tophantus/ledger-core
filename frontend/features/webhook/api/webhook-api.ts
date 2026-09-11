import {apiClient} from "@/lib/api/axios";
import {API_ENDPOINTS} from "@/lib/constants/api";
import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

import type {
    RegisterWebhookRequest,
    RegisterWebhookResponse,
    RotateWebhookSecretResponse,
    UpdateWebhookRequest,
    UpdateWebhookResponse,
    UpdateWebhookSubscriptionsRequest,
    Webhook,
    WebhookDelivery,
    WebhookDeliveryFilters,
    WebhookFilters,
} from "../types/webhook";

export const webhookApi = {
    register: async (
        request: RegisterWebhookRequest,
    ): Promise<ApiResponse<RegisterWebhookResponse>> => {
        const response =
            await apiClient.post<
                ApiResponse<RegisterWebhookResponse>
            >(
                API_ENDPOINTS.WEBHOOK.BASE,
                request,
            );

        return response.data;
    },

    update: async (
        webhookId: string,
        request: UpdateWebhookRequest,
    ): Promise<
        ApiResponse<UpdateWebhookResponse>
    > => {
        const response =
            await apiClient.patch<
                ApiResponse<UpdateWebhookResponse>
            >(
                API_ENDPOINTS.WEBHOOK.BY_ID(
                    webhookId,
                ),
                request,
            );

        return response.data;
    },

    updateSubscriptions: async (
        webhookId: string,
        request: UpdateWebhookSubscriptionsRequest,
    ): Promise<ApiResponse<void>> => {
        const response =
            await apiClient.put<
                ApiResponse<void>
            >(
                API_ENDPOINTS.WEBHOOK.SUBSCRIPTIONS(
                    webhookId,
                ),
                request,
            );

        return response.data;
    },

    rotateSecret: async (
        webhookId: string,
    ): Promise<
        ApiResponse<RotateWebhookSecretResponse>
    > => {
        const response =
            await apiClient.post<
                ApiResponse<RotateWebhookSecretResponse>
            >(
                API_ENDPOINTS.WEBHOOK.ROTATE_SECRET(
                    webhookId,
                ),
            );

        return response.data;
    },

    remove: async (
        webhookId: string,
    ): Promise<ApiResponse<void>> => {
        const response =
            await apiClient.delete<
                ApiResponse<void>
            >(
                API_ENDPOINTS.WEBHOOK.BY_ID(
                    webhookId,
                ),
            );

        return response.data;
    },

    getAll: async (
        filters: WebhookFilters = {},
    ): Promise<
        ApiResponse<PageResponse<Webhook>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<Webhook>>
            >(
                API_ENDPOINTS.WEBHOOK.BASE,
                {
                    params: filters,
                },
            );

        return response.data;
    },

    getById: async (
        webhookId: string,
    ): Promise<ApiResponse<Webhook>> => {
        const response =
            await apiClient.get<
                ApiResponse<Webhook>
            >(
                API_ENDPOINTS.WEBHOOK.BY_ID(
                    webhookId,
                ),
            );

        return response.data;
    },

    getDeliveries: async (
        webhookId: string,
        filters: WebhookDeliveryFilters = {},
    ): Promise<
        ApiResponse<PageResponse<WebhookDelivery>>
    > => {
        const response =
            await apiClient.get<
                ApiResponse<PageResponse<WebhookDelivery>>
            >(
                API_ENDPOINTS.WEBHOOK.DELIVERIES(
                    webhookId,
                ),
                {
                    params: filters,
                },
            );

        return response.data;
    },
};