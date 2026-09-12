export function InterestOverviewSkeleton() {
    return (
        <div className="divide-y divide-border">
            {Array.from({length: 5}).map(
                (_, index) => (
                    <div
                        key={index}
                        className="
                            grid
                            grid-cols-6
                            gap-4
                            px-4
                            py-4
                        "
                    >
                        {Array.from({
                            length: 6,
                        }).map((__, column) => (
                            <div
                                key={column}
                                className="
                                    h-4
                                    animate-pulse
                                    rounded
                                    bg-background-subtle
                                "
                            />
                        ))}
                    </div>
                ),
            )}
        </div>
    );
}