import type {
    ApiResponse,
    PageResponse,
} from "@/lib/api/types";

export enum WebhookStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
}

export enum WebhookDeliveryStatus {
    PENDING = "PENDING",
    RETRYING = "RETRYING",
    DELIVERED = "DELIVERED",
    FAILED = "FAILED",
}

export enum WebhookEventType {
    ACCOUNT_BALANCE_CHANGED =
        "ACCOUNT_BALANCE_CHANGED",
    TRANSACTION_COMPLETED =
        "TRANSACTION_COMPLETED",
    TRANSACTION_FAILED =
        "TRANSACTION_FAILED",
}

export interface RegisterWebhookRequest {
    accountId: string;
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