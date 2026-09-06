export function AccountDetailsSkeleton() {
    return (
        <div className="space-y-6">
            <div className="h-5 w-20 animate-pulse rounded bg-secondary" />

            <div className="rounded-lg border border-border bg-surface">
                <div className="border-b border-border px-6 py-5">
                    <div className="h-4 w-20 animate-pulse rounded bg-secondary" />

                    <div className="mt-2 h-6 w-32 animate-pulse rounded bg-secondary" />
                </div>

                <div className="grid gap-6 p-6 sm:grid-cols-2 lg:grid-cols-3">
                    {Array.from({length: 6}).map(
                        (_, index) => (
                            <div
                                key={index}
                                className="space-y-2"
                            >
                                <div className="h-4 w-20 animate-pulse rounded bg-secondary" />
                                <div className="h-5 w-32 animate-pulse rounded bg-secondary" />
                            </div>
                        ),
                    )}
                </div>
            </div>
        </div>
    );
}