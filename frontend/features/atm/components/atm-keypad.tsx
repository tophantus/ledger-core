import {ArrowLeft, Delete} from "lucide-react";
import {useTranslations} from "next-intl";

interface AtmKeypadProps {
    onKeyPress: (key: string) => void;
    onDelete: () => void;
    onClear: () => void;
    onBack: () => void;
    onNext: () => void;
}

const KEYPAD_KEYS = [
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
] as const;

export function AtmKeypad({
                              onKeyPress,
                              onDelete,
                              onClear,
                              onBack,
                              onNext,
                          }: AtmKeypadProps) {
    const t = useTranslations("atm");

    return (
        <div
            className="
                mt-5
                rounded-xl
                border
                border-gray-700
                bg-gray-800
                p-4
            "
        >
            <div
                className="
                    grid
                    grid-cols-3
                    gap-2
                "
            >
                {KEYPAD_KEYS.map((key) => (
                    <button
                        key={key}
                        type="button"
                        onClick={() =>
                            onKeyPress(key)
                        }
                        className="
                            h-11
                            rounded-lg
                            border
                            border-gray-600
                            bg-gray-700
                            text-sm
                            font-semibold
                            text-white
                            transition
                            hover:bg-gray-600
                            active:scale-95
                        "
                    >
                        {key}
                    </button>
                ))}

                <button
                    type="button"
                    onClick={onDelete}
                    className="
                        flex
                        h-11
                        items-center
                        justify-center
                        rounded-lg
                        bg-gray-700
                        text-white
                        hover:bg-gray-600
                    "
                >
                    <Delete className="h-4 w-4" />
                </button>

                <button
                    type="button"
                    onClick={() =>
                        onKeyPress("0")
                    }
                    className="
                        h-11
                        rounded-lg
                        border
                        border-gray-600
                        bg-gray-700
                        text-sm
                        font-semibold
                        text-white
                        hover:bg-gray-600
                    "
                >
                    0
                </button>

                <button
                    type="button"
                    onClick={onClear}
                    className="
                        h-11
                        rounded-lg
                        bg-yellow-600
                        text-xs
                        font-semibold
                        text-white
                        hover:bg-yellow-500
                    "
                >
                    {t("clear")}
                </button>
            </div>

            <div
                className="
                    mt-3
                    grid
                    grid-cols-2
                    gap-2
                "
            >
                <button
                    type="button"
                    onClick={onBack}
                    className="
                        flex
                        h-11
                        items-center
                        justify-center
                        gap-2
                        rounded-lg
                        bg-gray-700
                        text-sm
                        font-medium
                        text-white
                        hover:bg-gray-600
                    "
                >
                    <ArrowLeft className="h-4 w-4" />
                    {t("back")}
                </button>

                <button
                    type="button"
                    onClick={onNext}
                    className="
                        h-11
                        rounded-lg
                        bg-green-600
                        text-sm
                        font-semibold
                        text-white
                        transition
                        hover:bg-green-500
                    "
                >
                    {t("continue")}
                </button>
            </div>
        </div>
    );
}