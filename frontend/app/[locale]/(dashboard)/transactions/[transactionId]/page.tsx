"use client";

import {useEffect, useState} from "react";
import {useParams} from "next/navigation";
import {useTranslations} from "next-intl";

import {TransactionDetail} from "@/features/transaction/components/transaction-detail";
import {useTransaction} from "@/features/transaction/hooks/use-transaction";
import {Transaction} from "@/features/transaction/types/transaction";

export default function TransactionDetailPage() {
    const tErrors = useTranslations("errors");

    const params = useParams<{
        transactionId: string;
    }>();

    const transactionId =
        params.transactionId;

    const {getTransaction} =
        useTransaction();

    const [isLoading, setIsLoading] =
        useState(true);

    const [transaction, setTransaction] =
        useState<Transaction | null>(null);

    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        let mounted = true;

        const loadTransaction = async () => {
            try {
                const response =
                    await getTransaction(
                        transactionId,
                    );

                if (!mounted) {
                    return;
                }

                if (!response.success) {
                    setError(
                        response.code &&
                        tErrors.has(
                            response.code,
                        )
                            ? tErrors(
                                response.code,
                            )
                            : tErrors(
                                "fallback",
                            ),
                    );

                    return;
                }

                setTransaction(
                    response.data,
                );
            } catch {
                if (mounted) {
                    setError(
                        tErrors("fallback"),
                    );
                }
            } finally {
                if (mounted) {
                    setIsLoading(false);
                }
            }
        };

        void loadTransaction();

        return () => {
            mounted = false;
        };
    }, [
        transactionId,
        getTransaction,
        tErrors,
    ]);

    if (isLoading) {
        return (
            <section className="space-y-6">
                <div className="h-5 w-20 animate-pulse rounded bg-secondary" />

                <div className="space-y-2">
                    <div className="h-8 w-48 animate-pulse rounded bg-secondary" />
                    <div className="h-4 w-72 animate-pulse rounded bg-secondary" />
                </div>

                <div className="rounded-lg border border-border bg-surface p-6">
                    <div className="space-y-5">
                        {Array.from({
                            length: 7,
                        }).map((_, index) => (
                            <div
                                key={index}
                                className="flex justify-between gap-6"
                            >
                                <div className="h-4 w-24 animate-pulse rounded bg-secondary" />
                                <div className="h-4 w-40 animate-pulse rounded bg-secondary" />
                            </div>
                        ))}
                    </div>
                </div>
            </section>
        );
    }

    if (error || !transaction) {
        return (
            <section className="rounded-lg border border-border bg-surface p-8 text-center">
                <p className="text-sm text-danger">
                    {error ??
                        tErrors("fallback")}
                </p>
            </section>
        );
    }

    return (
        <TransactionDetail
            transaction={transaction}
        />
    );
}