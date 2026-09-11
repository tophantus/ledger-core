export function WebhookDeliveryListSkeleton() {
    return (
        <div className="
            overflow-hidden
            rounded-lg
            border
            border-border
            bg-surface
        ">
            <div className="overflow-x-auto">
                <table className="
                    w-full
                    min-w-[1100px]
                ">
                    <thead>
                    <tr className="
                        border-b
                        border-border
                        bg-background-subtle
                    ">
                        {Array.from({
                            length: 6,
                        }).map(
                            (_, index) => (
                                <th
                                    key={
                                        index
                                    }
                                    className="
                                        px-4
                                        py-3
                                    "
                                >
                                    <div className="
                                        h-3
                                        w-20
                                        animate-pulse
                                        rounded
                                        bg-secondary"
                                    />
                                </th>
                            ),
                        )}
                    </tr>
                    </thead>

                    <tbody>
                    {Array.from({
                        length: 5,
                    }).map(
                        (_, rowIndex) => (
                            <tr
                                key={
                                    rowIndex
                                }
                                className="
                                         border-b
                                         border-border
                                         last:border-b-0
                                        "
                            >
                                {Array.from({
                                    length: 6,
                                }).map(
                                    (
                                        __,
                                        cellIndex,
                                    ) => (
                                        <td
                                            key={
                                                cellIndex
                                            }
                                            className="
                                         px-4
                                         py-4
                                        "
                                        >
                                            <div className="
                                         h-4
                                         w-24
                                         animate-pulse
                                         rounded
                                         bg-secondary
                                        " />
                                        </td>
                                    ),
                                )}
                            </tr>
                        ),
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}