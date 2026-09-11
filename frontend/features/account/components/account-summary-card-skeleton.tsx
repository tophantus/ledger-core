export function AccountSummaryCardSkeleton() {
    return (
        <div className="rounded-lg border border-border bg-surface">
            <div className="flex items-start justify-between gap-4 p-6">
                <div className="space-y-2">
                    <div className="h-3 w-24 animate-pulse rounded bg-secondary" />
                    <div className="h-5 w-36 animate-pulse rounded bg-secondary" />
                </div>

                <div className="h-6 w-16 animate-pulse rounded-full bg-secondary" />
            </div>

            <div className="border-t border-border px-6 py-5">
                <div className="space-y-2">
                    <div className="h-3 w-16 animate-pulse rounded bg-secondary" />
                    <div className="h-7 w-40 animate-pulse rounded bg-secondary" />
                </div>
            </div>
        </div>
    );
}