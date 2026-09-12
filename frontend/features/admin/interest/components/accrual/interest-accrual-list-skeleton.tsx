export function InterestAccrualListSkeleton() {
    return (
        <div className="
            overflow-x-auto
            rounded-md
            border
            border-border
        ">
            <table className="
                w-full
                min-w-[1100px]
            ">
                <tbody>
                {Array.from(
                    {length: 8},
                    (_, index) => (
                        <tr
                            key={index}
                            className="
                                    border-b
                                    border-border
                                    last:border-b-0
                                "
                        >
                            {Array.from(
                                {length: 8},
                                (_, cellIndex) => (
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
                                                w-full
                                                max-w-32
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
    );
}