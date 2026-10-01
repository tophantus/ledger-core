export function CreditStatementsSkeleton() {
    return (
        <div className="space-y-3">
            {Array.from({length: 3}).map(
                (_, index) => (
                    <div
                        key={index}
                        className="
                            animate-pulse rounded-xl
                            border border-border
                            bg-surface p-4 sm:p-5
                        "
                    >
                        <div className="flex items-start justify-between gap-4">
                            <div className="space-y-2">
                                <div className="h-4 w-40 rounded bg-background-subtle" />
                                <div className="h-3 w-28 rounded bg-background-subtle" />
                            </div>

                            <div className="h-6 w-20 rounded-full bg-background-subtle" />
                        </div>

                        <div
                            className="
                                mt-5 grid gap-4
                                sm:grid-cols-2
                                lg:grid-cols-4
                            "
                        >
                            {Array.from({
                                length: 4,
                            }).map(
                                (_, itemIndex) => (
                                    <div
                                        key={
                                            itemIndex
                                        }
                                        className="space-y-2"
                                    >
                                        <div className="h-3 w-24 rounded bg-background-subtle" />
                                        <div className="h-4 w-32 rounded bg-background-subtle" />
                                    </div>
                                ),
                            )}
                        </div>

                        <div className="mt-5 border-t border-border pt-4">
                            <div className="h-3 w-32 rounded bg-background-subtle" />
                        </div>
                    </div>
                ),
            )}
        </div>
    );
}