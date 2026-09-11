"use client";

import {
    useEffect,
    useMemo,
    useState,
} from "react";
import {useTranslations} from "next-intl";
import {useParams, useRouter} from "next/navigation";

import {Button} from "@/components/ui/button";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";

import {useWebhook} from "../hooks/use-webhook";
import type {Webhook} from "../types/webhook";

import {WebhookDetailsSummary} from "./webhook-details-summary";
import {WebhookDeliveryList} from "./webhook-delivery-list";
import {AccountSummary} from "@/features/account/types/account";

export default function WebhookDetailsPage() {
    const params = useParams<{
        webhookId: string;
    }>();

    const webhookId =
        params.webhookId;

    const t =
        useTranslations("webhook");

    const tErrors =
        useTranslations("errors");

    const router =
        useRouter();

    const {
        getWebhook,
    } = useWebhook();

    const {
        getMyAccounts,
    } = useMyAccounts();

    const [
        webhook,
        setWebhook,
    ] = useState<Webhook | null>(null);

    const [
        accounts,
        setAccounts,
    ] = useState<AccountSummary[]>([]);

    const [
        isLoading,
        setIsLoading,
    ] = useState(true);

    const [
        error,
        setError,
    ] = useState<string | null>(null);

    useEffect(() => {
        let mounted = true;

        const load = async () => {
            setIsLoading(true);
            setError(null);

            try {
                const [
                    webhookResponse,
                    accountsResponse,
                ] = await Promise.all([
                    getWebhook(webhookId),
                    getMyAccounts(),
                ]);

                if (!mounted) {
                    return;
                }

                if (
                    !webhookResponse.success
                ) {
                    if (
                        webhookResponse.code &&
                        tErrors.has(
                            webhookResponse.code,
                        )
                    ) {
                        setError(
                            tErrors(
                                webhookResponse.code,
                            ),
                        );
                    } else {
                        setError(
                            tErrors(
                                "fallback",
                            ),
                        );
                    }

                    return;
                }

                setWebhook(
                    webhookResponse.data,
                );

                if (
                    accountsResponse.success
                ) {
                    setAccounts(
                        accountsResponse.data,
                    );
                }
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

        void load();

        return () => {
            mounted = false;
        };
    }, [
        webhookId,
        getWebhook,
        getMyAccounts,
        tErrors,
    ]);

    const accountNo =
        useMemo(
            () =>
                webhook
                    ? accounts.find(
                        (account) =>
                            account.id ===
                            webhook.accountId,
                    )?.accountNo
                    : undefined,
            [
                accounts,
                webhook,
            ],
        );

    if (isLoading) {
        return (
            <section className="
                mx-auto
                w-full
                space-y-6
            ">
                <div className="
                    h-8
                    w-48
                    animate-pulse
                    rounded
                    bg-secondary"
                />

                <div className="
                     h-48
                     animate-pulse
                     rounded-lg
                     bg-secondary
                    "/>

                <div className="
                     h-8
                     w-40
                     animate-pulse
                     rounded
                     bg-secondary
                    "/>

                <div className="
                     h-96
                     animate-pulse
                     rounded-lg
                     bg-secondary
                    "/>
            </section>
        );
    }

    if (error || !webhook) {
        return (
            <section className="
                     mx-auto
                     w-full
                     space-y-4
                    ">
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
                        {error ??
                            tErrors(
                                "fallback",
                            )}
                    </p>
                </div>

                <Button
                    type="button"
                    variant="outline"
                    onClick={() =>
                        router.back()
                    }
                >
                    {t(
                        "details.back",
                    )}
                </Button>
            </section>
        );
    }

    return (
        <section className="
                     mx-auto
                     w-full
                     space-y-6
                    ">
            <div className="
                     flex
                     items-start
                     justify-between
                     gap-4
                    ">
                <div>
                    <h1 className="
                     text-2xl
                     font-semibold
                     text-primary
                    ">
                        {t(
                            "details.title",
                        )}
                    </h1>

                    <p className="
                     mt-1
                     text-sm
                     text-muted
                    ">
                        {t(
                            "details.description",
                        )}
                    </p>
                </div>

                <Button
                    type="button"
                    variant="outline"
                    onClick={() =>
                        router.back()
                    }
                >
                    {t(
                        "details.back",
                    )}
                </Button>
            </div>

            <WebhookDetailsSummary
                webhook={webhook}
                accountNo={accountNo}
            />

            <WebhookDeliveryList
                webhookId={
                    webhook.id
                }
            />
        </section>
    );
}