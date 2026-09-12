export function InterestPostingListSkeleton() {
    return (
        <div className="
            overflow-hidden
            rounded-lg
            border
            border-border
            bg-surface
        ">
            <div className="overflow-x-auto">
                <table className="w-full">
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
                                            h-4
                                            w-24
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
                        (_, rowIndex) => (
                            <tr
                                key={rowIndex}
                                className="
                                        border-b
                                        border-border
                                    "
                            >
                                {Array.from(
                                    {length: 7},
                                    (_, columnIndex) => (
                                        <td
                                            key={
                                                columnIndex
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