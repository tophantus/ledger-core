export function WebhookListSkeleton() {
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
                    min-w-[1000px]
                ">
                    <thead>
                    <tr className="
                        border-b
                        border-border
                    ">
                        {Array.from(
                            {length: 7},
                            (_, index) => (
                                <th
                                    key={index}
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
                                        bg-background-subtle
                                    " />
                                </th>
                            ),
                        )}
                    </tr>
                    </thead>

                    <tbody>
                    {Array.from(
                        {length: 8},
                        (_, row) => (
                            <tr
                                key={row}
                                className="
                                    border-b
                                    border-border
                                    last:border-b-0
                                "
                            >
                                {Array.from(
                                    {length: 7},
                                    (_, column) => (
                                        <td
                                            key={
                                                column
                                            }
                                            className="
                                                px-4
                                                py-4
                                            "
                                        >
                                            <div className="
                                                h-4
                                                w-full
                                                max-w-32
                                                animate-pulse
                                                rounded
                                                bg-background-subtle
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