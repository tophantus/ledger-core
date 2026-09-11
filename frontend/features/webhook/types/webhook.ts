import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

export type WebhookStatus =
    | "ACTIVE"
    | "INACTIVE";

export type WebhookDeliveryStatus =
    | "PENDING"
    | "RETRYING"
    | "DELIVERED"
    | "FAILED";

export type WebhookEventType =
    | "ACCOUNT_BALANCE_CHANGED"
    | "TRANSACTION_COMPLETED"
    | "TRANSACTION_FAILED";

export interface RegisterWebhookRequest {
    url: string;
    eventTypes: WebhookEventType[];
}

export interface RegisterWebhookResponse {
    webhookId: string;
    accountId: string;
    url: string;
    secret: string;
    status: WebhookStatus;
    eventTypes: WebhookEventType[];
    createdAt: string;
}

export interface UpdateWebhookRequest {
    url: string;
}

export interface UpdateWebhookResponse {
    id: string;
    accountId: string;
    url: string;
    status: WebhookStatus;
    updatedAt: string;
}

export interface UpdateWebhookSubscriptionsRequest {
    eventTypes: WebhookEventType[];
}

export interface RotateWebhookSecretResponse {
    webhookId: string;
    secret: string;
    rotatedAt: string;
}

export interface Webhook {
    id: string;
    accountId: string;
    url: string;
    status: WebhookStatus;
    eventTypes: WebhookEventType[];
    createdAt: string;
    updatedAt: string;
}

export interface WebhookDelivery {
    id: string;
    eventId: string;
    eventType: WebhookEventType;
    status: WebhookDeliveryStatus;
    nextAttemptAt: string | null;
    deliveredAt: string | null;
    lastError: string | null;
    createdAt: string;
}

export interface WebhookFilters {
    accountId?: string;
    page?: number;
    size?: number;
}

export interface WebhookDeliveryFilters {
    status?: WebhookDeliveryStatus;
    eventType?: WebhookEventType;
    page?: number;
    size?: number;
}

export type RegisterWebhookApiResponse =
    ApiResponse<RegisterWebhookResponse>;

export type UpdateWebhookApiResponse =
    ApiResponse<UpdateWebhookResponse>;

export type UpdateWebhookSubscriptionsApiResponse =
    ApiResponse<void>;

export type DeleteWebhookApiResponse =
    ApiResponse<void>;

export type RotateWebhookSecretApiResponse =
    ApiResponse<RotateWebhookSecretResponse>;

export type WebhookApiResponse =
    ApiResponse<Webhook>;

export type WebhookPageApiResponse =
    ApiResponse<PageResponse<Webhook>>;

export type WebhookDeliveryPageApiResponse =
    ApiResponse<PageResponse<WebhookDelivery>>;