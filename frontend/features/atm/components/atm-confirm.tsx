import {ArrowLeft} from "lucide-react";
import {useTranslations} from "next-intl";

interface AtmConfirmProps {
    withdrawalReference: string;
    amount: string;
    isLoading: boolean;
    onBack: () => void;
    onExecute: () => void;
}

export function AtmConfirm({
                               withdrawalReference,
                               amount,
                               isLoading,
                               onBack,
                               onExecute,
                           }: AtmConfirmProps) {
    const t = useTranslations("atm");

    return (
        <div
            className="
                mt-5
                grid
                grid-cols-2
                gap-3
            "
        >
            <button
                type="button"
                onClick={onBack}
                disabled={isLoading}
                className="
                    flex
                    items-center
                    justify-center
                    gap-2
                    rounded-lg
                    bg-gray-700
                    px-4
                    py-3
                    text-sm
                    font-medium
                    text-white
                    hover:bg-gray-600
                    disabled:opacity-50
                "
            >
                <ArrowLeft className="h-4 w-4" />
                {t("back")}
            </button>

            <button
                type="button"
                onClick={onExecute}
                disabled={isLoading}
                className="
                    rounded-lg
                    bg-green-600
                    px-4
                    py-3
                    text-sm
                    font-semibold
                    text-white
                    hover:bg-green-500
                    disabled:opacity-50
                "
            >
                {isLoading
                    ? t("processing")
                    : t("withdraw")}
            </button>
        </div>
    );
}