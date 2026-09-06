export function TransactionSkeleton() {
    return (
        <div className="flex items-center justify-between gap-4 py-4">
            <div className="flex items-center gap-3">
                <div className="h-10 w-10 animate-pulse rounded-full bg-secondary" />

                <div className="space-y-2">
                    <div className="h-4 w-24 animate-pulse rounded bg-secondary" />

                    <div className="h-3 w-32 animate-pulse rounded bg-secondary" />
                </div>
            </div>

            <div className="space-y-2">
                <div className="ml-auto h-4 w-24 animate-pulse rounded bg-secondary" />

                <div className="ml-auto h-3 w-16 animate-pulse rounded bg-secondary" />
            </div>
        </div>
    );
}