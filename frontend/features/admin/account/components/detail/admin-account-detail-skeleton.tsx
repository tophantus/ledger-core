export function AdminAccountDetailSkeleton() {
    return (
        <div className="space-y-6 animate-pulse">
            {/* Header */}
            <div
                className="
                    flex
                    flex-col
                    gap-4
                    sm:flex-row
                    sm:items-center
                    sm:justify-between
                "
            >
                <div className="flex items-center gap-4">
                    <div
                        className="
                            h-10
                            w-20
                            rounded-md
                            bg-background
                        "
                    />

                    <div className="space-y-2">
                        <div
                            className="
                                h-7
                                w-48
                                rounded
                                bg-background
                            "
                        />

                        <div
                            className="
                                h-4
                                w-32
                                rounded
                                bg-background
                            "
                        />
                    </div>
                </div>

                <div
                    className="
                        h-10
                        w-28
                        rounded-md
                        bg-background
                    "
                />
            </div>

            {/* Account information */}
            <section
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                "
            >
                <div className="border-b border-border px-6 py-4">
                    <div
                        className="
                            h-5
                            w-40
                            rounded
                            bg-background
                        "
                    />
                </div>

                <div
                    className="
                        grid
                        gap-6
                        p-6
                        sm:grid-cols-2
                        lg:grid-cols-3
                    "
                >
                    {Array.from({length: 7}).map(
                        (_, index) => (
                            <div
                                key={index}
                                className="space-y-2"
                            >
                                <div
                                    className="
                                        h-4
                                        w-24
                                        rounded
                                        bg-background
                                    "
                                />

                                <div
                                    className="
                                        h-5
                                        w-36
                                        rounded
                                        bg-background
                                    "
                                />
                            </div>
                        ),
                    )}
                </div>
            </section>

            {/* Owner information */}
            <section
                className="
                    rounded-lg
                    border
                    border-border
                    bg-surface
                "
            >
                <div className="border-b border-border px-6 py-4">
                    <div
                        className="
                            h-5
                            w-36
                            rounded
                            bg-background
                        "
                    />
                </div>

                <div className="flex items-center gap-4 p-6">
                    <div
                        className="
                            h-14
                            w-14
                            shrink-0
                            rounded-full
                            bg-background
                        "
                    />

                    <div className="space-y-2">
                        <div
                            className="
                                h-5
                                w-32
                                rounded
                                bg-background
                            "
                        />

                        <div
                            className="
                                h-4
                                w-48
                                rounded
                                bg-background
                            "
                        />

                        <div
                            className="
                                h-3
                                w-64
                                rounded
                                bg-background
                            "
                        />
                    </div>
                </div>
            </section>
        </div>
    );
}