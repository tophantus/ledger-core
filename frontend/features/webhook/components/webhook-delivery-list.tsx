"use client";

import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import {useTranslations} from "next-intl";

import {useWebhookDeliveries} from "../hooks/use-webhook-deliveries";
import type {
    WebhookDelivery,
    WebhookDeliveryFilters,
} from "../types/webhook";

import {WebhookDeliveryListFilters} from "./webhook-delivery-list-filters";
import {WebhookDeliveryListTable} from "./webhook-delivery-list-table";
import {WebhookDeliveryListSkeleton} from "./webhook-delivery-list-skeleton";
import {WebhookDeliveryListEmpty} from "./webhook-delivery-list-empty";
import {WebhookDeliveryListPagination} from "./webhook-delivery-list-pagination";

const PAGE_SIZE = 20;

interface WebhookDeliveryListProps {
    webhookId: string;
}

export function WebhookDeliveryList({
                                        webhookId,
                                    }: WebhookDeliveryListProps) {
    const t =
        useTranslations("webhook");

    const tErrors =
        useTranslations("errors");

    const {
        getWebhookDeliveries,
    } = useWebhookDeliveries();

    const [
        status,
        setStatus,
    ] = useState<
        WebhookDeliveryFilters["status"]
    >(undefined);

    const [
        eventType,
        setEventType,
    ] = useState<
        WebhookDeliveryFilters["eventType"]
    >(undefined);

    const [
        page,
        setPage,
    ] = useState(0);

    const [
        deliveries,
        setDeliveries,
    ] = useState<WebhookDelivery[]>([]);

    const [
        totalPages,
        setTotalPages,
    ] = useState(0);

    const [
        isLoading,
        setIsLoading,
    ] = useState(true);

    const [
        error,
        setError,
    ] = useState<string | null>(null);

    const filters =
        useMemo<WebhookDeliveryFilters>(
            () => ({
                status,
                eventType,
                page,
                size: PAGE_SIZE,
            }),
            [
                status,
                eventType,
                page,
            ],
        );

    const getErrorMessage =
        useCallback(
            (code?: string) => {
                if (
                    code &&
                    tErrors.has(code)
                ) {
                    return tErrors(code);
                }

                return tErrors(
                    "fallback",
                );
            },
            [tErrors],
        );

    useEffect(() => {
        let mounted = true;

        const loadDeliveries =
            async () => {
                setIsLoading(true);
                setError(null);

                try {
                    const response =
                        await getWebhookDeliveries(
                            webhookId,
                            filters,
                        );

                    if (!mounted) {
                        return;
                    }

                    if (!response.success) {
                        setError(
                            getErrorMessage(
                                response.code,
                            ),
                        );

                        return;
                    }

                    setDeliveries(
                        response.data.content,
                    );

                    setTotalPages(
                        response.data.totalPages,
                    );
                } catch {
                    if (mounted) {
                        setError(
                            tErrors(
                                "fallback",
                            ),
                        );
                    }
                } finally {
                    if (mounted) {
                        setIsLoading(false);
                    }
                }
            };

        void loadDeliveries();

        return () => {
            mounted = false;
        };
    }, [
        webhookId,
        status,
        eventType,
        page,
        getWebhookDeliveries,
        getErrorMessage,
        tErrors,
    ]);

    const handleFilterChange = (
        nextFilters: WebhookDeliveryFilters,
    ) => {
        setStatus(
            nextFilters.status,
        );

        setEventType(
            nextFilters.eventType,
        );

        setPage(
            nextFilters.page ?? 0,
        );
    };

    return (
        <section className="
            space-y-4
        ">
            <div>
                <h2 className="
                    text-lg
                    font-semibold
                    text-foreground
                ">
                    {t(
                        "deliveries.title",
                    )}
                </h2>

                <p className="
                    mt-1
                    text-sm
                    text-muted
                ">
                    {t(
                        "deliveries.description",
                    )}
                </p>
            </div>

            <WebhookDeliveryListFilters
                filters={filters}
                onChange={
                    handleFilterChange
                }
            />

            {isLoading ? (
                <WebhookDeliveryListSkeleton />
            ) : error ? (
                <div className="
                    rounded-lg
                    border
                    border-danger
                    bg-surface
                    p-4
                ">
                    <p className="
                        text-sm
                        text-danger
                    ">
                        {error}
                    </p>
                </div>
            ) : deliveries.length === 0 ? (
                <WebhookDeliveryListEmpty />
            ) : (
                <>
                    <WebhookDeliveryListTable
                        deliveries={
                            deliveries
                        }
                    />

                    {totalPages > 1 && (
                        <WebhookDeliveryListPagination
                            page={page}
                            totalPages={
                                totalPages
                            }
                            onPageChange={
                                setPage
                            }
                        />
                    )}
                </>
            )}
        </section>
    );
}