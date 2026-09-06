export function AccountCardSkeleton() {
    return (
        <div
            aria-hidden="true"
            className="
                animate-pulse
                rounded-lg
                border
                border-border
                bg-surface
                p-5
                shadow-sm
            "
        >
            <div className="flex items-start justify-between">
                <div className="space-y-2">
                    <div className="h-3 w-24 rounded bg-surface-subtle" />
                    <div className="h-5 w-32 rounded bg-surface-subtle" />
                </div>

                <div className="h-6 w-12 rounded-full bg-surface-subtle" />
            </div>

            <div className="mt-6 space-y-2">
                <div className="h-3 w-16 rounded bg-surface-subtle" />
                <div className="h-8 w-40 rounded bg-surface-subtle" />
            </div>

            <div className="mt-5 border-t border-border pt-4">
                <div className="h-3 w-14 rounded bg-surface-subtle" />

                <div className="mt-2 h-4 w-20 rounded bg-surface-subtle" />
            </div>
        </div>
    );
}