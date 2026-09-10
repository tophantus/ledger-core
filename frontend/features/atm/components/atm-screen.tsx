import {
    CheckCircle2,
    LockKeyhole,
} from "lucide-react";
import {useTranslations} from "next-intl";

import type {
    ExecuteWithdrawalResponse,
} from "../types/atm";

type AtmStep =
    | "REFERENCE"
    | "CODE"
    | "AMOUNT"
    | "CONFIRM"
    | "SUCCESS"
    | "ERROR";

interface AtmScreenProps {
    step: AtmStep;
    input: string;
    error: string | null;
    withdrawalReference: string;
    amount: string;
    result: ExecuteWithdrawalResponse | null;
}

export function AtmScreen({
                              step,
                              input,
                              error,
                              withdrawalReference,
                              amount,
                              result,
                          }: AtmScreenProps) {
    const t = useTranslations("atm");

    const getTitle = () => {
        switch (step) {
            case "REFERENCE":
                return t("enterReference");

            case "CODE":
                return t("enterCode");

            case "AMOUNT":
                return t("enterAmount");

            case "CONFIRM":
                return t("confirmTitle");

            case "SUCCESS":
                return t("successTitle");

            case "ERROR":
                return t("errorTitle");
        }
    };

    const getDescription = () => {
        switch (step) {
            case "REFERENCE":
                return t(
                    "enterReferenceDescription",
                );

            case "CODE":
                return t(
                    "enterCodeDescription",
                );

            case "AMOUNT":
                return t(
                    "enterAmountDescription",
                );

            case "CONFIRM":
                return t(
                    "confirmDescription",
                );

            case "SUCCESS":
                return t(
                    "successDescription",
                );

            default:
                return "";
        }
    };

    return (
        <div
            className="
                rounded-xl
                border
                border-blue-400/30
                bg-gradient-to-br
                from-blue-700
                to-blue-950
                p-5
                text-white
                shadow-inner
            "
        >
            <div
                className="
                    flex
                    items-center
                    gap-2
                    text-xs
                    text-blue-100
                "
            >
                <LockKeyhole className="h-4 w-4" />
                LedgerCore ATM
            </div>

            <div className="mt-8 text-center">
                <p
                    className="
                        text-lg
                        font-semibold
                    "
                >
                    {getTitle()}
                </p>

                <p
                    className="
                        mt-1
                        text-xs
                        text-blue-100
                    "
                >
                    {getDescription()}
                </p>
            </div>

            {step === "CONFIRM" && (
                <div className="mt-6 space-y-3">
                    <div
                        className="
                            rounded-lg
                            bg-black/20
                            p-3
                        "
                    >
                        <p
                            className="
                                text-[10px]
                                text-blue-200
                            "
                        >
                            {t("reference")}
                        </p>

                        <p
                            className="
                                mt-1
                                break-all
                                font-mono
                                text-sm
                            "
                        >
                            {withdrawalReference}
                        </p>
                    </div>

                    <div
                        className="
                            rounded-lg
                            bg-black/20
                            p-3
                        "
                    >
                        <p
                            className="
                                text-[10px]
                                text-blue-200
                            "
                        >
                            {t("amount")}
                        </p>

                        <p
                            className="
                                mt-1
                                text-2xl
                                font-bold
                            "
                        >
                            {amount}
                        </p>
                    </div>
                </div>
            )}

            {(step === "REFERENCE" ||
                step === "CODE" ||
                step === "AMOUNT") && (
                <div
                    className="
                        mt-6
                        rounded-lg
                        border
                        border-white/20
                        bg-black/20
                        px-4
                        py-3
                        text-center
                    "
                >
                    <p
                        className="
                            min-h-7
                            break-all
                            font-mono
                            text-lg
                            tracking-wider
                        "
                    >
                        {input || " "}
                    </p>
                </div>
            )}

            {error && step !== "ERROR" && (
                <p
                    className="
                        mt-3
                        text-center
                        text-xs
                        text-red-200
                    "
                >
                    {error}
                </p>
            )}

            {step === "SUCCESS" && (
                <div className="py-8 text-center">
                    <CheckCircle2
                        className="
                            mx-auto
                            h-12
                            w-12
                            text-green-300
                        "
                    />

                    <p
                        className="
                            mt-4
                            text-lg
                            font-semibold
                        "
                    >
                        {t("successTitle")}
                    </p>

                    <p
                        className="
                            mt-2
                            text-xs
                            text-blue-100
                        "
                    >
                        {t("successDescription")}
                    </p>

                    {result && (
                        <p
                            className="
                                mt-3
                                break-all
                                font-mono
                                text-xs
                                text-blue-200
                            "
                        >
                            {result.executionId}
                        </p>
                    )}
                </div>
            )}

            {step === "ERROR" && (
                <div className="py-8 text-center">
                    <p
                        className="
                            text-lg
                            font-semibold
                        "
                    >
                        {t("errorTitle")}
                    </p>

                    <p
                        className="
                            mt-2
                            text-xs
                            text-red-200
                        "
                    >
                        {error}
                    </p>
                </div>
            )}
        </div>
    );
}