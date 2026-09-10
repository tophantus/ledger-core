"use client";

import {LockKeyhole, X} from "lucide-react";
import {useState} from "react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";

import {useExecuteWithdrawal} from "../hooks/use-execute-withdrawal";
import type {ExecuteWithdrawalResponse} from "../types/atm";

import {AtmConfirm} from "./atm-confirm";
import {AtmKeypad} from "./atm-keypad";
import {AtmScreen} from "./atm-screen";
import {AtmSlots} from "./atm-slots";

type AtmStep =
    | "LOOKUP_CODE"
    | "CODE"
    | "AMOUNT"
    | "CONFIRM"
    | "SUCCESS"
    | "ERROR";

export function AtmSimulator() {
    const t = useTranslations("atm");

    const {executeWithdrawal} =
        useExecuteWithdrawal();

    const [open, setOpen] =
        useState(false);

    const [step, setStep] =
        useState<AtmStep>("LOOKUP_CODE");

    const [lookupCode, setLookupCode] =
        useState("");

    const [withdrawalCode, setWithdrawalCode] =
        useState("");

    const [amount, setAmount] =
        useState("");

    const [input, setInput] =
        useState("");

    const [isLoading, setIsLoading] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const [result, setResult] =
        useState<ExecuteWithdrawalResponse | null>(
            null,
        );

    const handleOpen = () => {
        setOpen(true);
        setStep("LOOKUP_CODE");
        setLookupCode("");
        setWithdrawalCode("");
        setAmount("");
        setInput("");
        setError(null);
        setResult(null);
    };

    const handleClose = () => {
        if (isLoading) {
            return;
        }

        setOpen(false);
    };

    const handleKeyPress = (key: string) => {
        setInput(
            (current) => current + key,
        );
    };

    const handleDelete = () => {
        setInput(
            (current) =>
                current.slice(0, -1),
        );
    };

    const handleClear = () => {
        setInput("");
    };

    const handleNext = () => {
        setError(null);

        if (step === "LOOKUP_CODE") {
            if (!input.trim()) {
                setError(
                    t("lookupCodeRequired"),
                );
                return;
            }

            setLookupCode(input.trim());
            setInput("");
            setStep("CODE");
            return;
        }

        if (step === "CODE") {
            if (input.length !== 6) {
                setError(
                    t("invalidCode"),
                );
                return;
            }

            setWithdrawalCode(input);
            setInput("");
            setStep("AMOUNT");
            return;
        }

        if (step === "AMOUNT") {
            if (
                !input ||
                Number(input) <= 0
            ) {
                setError(
                    t("invalidAmount"),
                );
                return;
            }

            setAmount(input);
            setInput("");
            setStep("CONFIRM");
        }
    };

    const handleBack = () => {
        setError(null);

        if (step === "CODE") {
            setStep("LOOKUP_CODE");
            setInput(lookupCode);
            return;
        }

        if (step === "AMOUNT") {
            setStep("CODE");
            setInput(withdrawalCode);
            return;
        }

        if (step === "CONFIRM") {
            setStep("AMOUNT");
            setInput(amount);
        }
    };

    const handleExecute = async () => {
        if (isLoading) {
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const response =
                await executeWithdrawal({
                    lookupCode,
                    withdrawalCode,
                    amount,
                });

            if (!response.success) {
                setError(
                    response.message ||
                    t("executeFailed"),
                );
                setStep("ERROR");
                return;
            }

            setResult(response.data);
            setStep("SUCCESS");
        } catch {
            setError(
                t("executeFailed"),
            );
            setStep("ERROR");
        } finally {
            setIsLoading(false);
        }
    };

    const isInputStep =
        step === "LOOKUP_CODE" ||
        step === "CODE" ||
        step === "AMOUNT";

    return (
        <>
            <Button
                type="button"
                variant="outline"
                onClick={handleOpen}
            >
                <LockKeyhole className="h-4 w-4" />
                {t("openAtm")}
            </Button>

            {open && (
                <div
                    className="
                        fixed
                        inset-0
                        z-50
                        flex
                        items-center
                        justify-center
                        bg-black/60
                        p-4
                        backdrop-blur-sm
                    "
                >
                    <div
                        className="
                            relative
                            w-full
                            max-w-lg
                            overflow-hidden
                            rounded-2xl
                            border
                            border-border
                            bg-surface
                            shadow-2xl
                        "
                    >
                        {/* Header */}
                        <div
                            className="
                                flex
                                items-center
                                justify-between
                                border-b
                                border-border
                                px-5
                                py-4
                            "
                        >
                            <div>
                                <h2
                                    className="
                                        text-lg
                                        font-semibold
                                        text-primary
                                    "
                                >
                                    {t("title")}
                                </h2>

                                <p
                                    className="
                                        mt-0.5
                                        text-xs
                                        text-muted
                                    "
                                >
                                    {t("subtitle")}
                                </p>
                            </div>

                            <button
                                type="button"
                                onClick={handleClose}
                                disabled={isLoading}
                                className="
                                    rounded-full
                                    p-2
                                    text-muted
                                    transition
                                    hover:bg-background-subtle
                                    hover:text-text-primary
                                    disabled:opacity-50
                                "
                            >
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        {/* ATM body */}
                        <div
                            className="
                                bg-background-subtle
                                p-5
                            "
                        >
                            <div
                                className="
                                    mx-auto
                                    max-w-sm
                                    rounded-2xl
                                    border
                                    border-gray-700
                                    bg-gray-900
                                    p-4
                                    shadow-xl
                                "
                            >
                                <AtmScreen
                                    step={step}
                                    input={input}
                                    error={error}
                                    lookupCode={lookupCode}
                                    amount={amount}
                                    result={result}
                                />

                                {isInputStep && (
                                    <AtmKeypad
                                        onKeyPress={
                                            handleKeyPress
                                        }
                                        onDelete={
                                            handleDelete
                                        }
                                        onClear={
                                            handleClear
                                        }
                                        onBack={
                                            handleBack
                                        }
                                        onNext={
                                            handleNext
                                        }
                                    />
                                )}

                                {step === "CONFIRM" && (
                                    <AtmConfirm
                                        lookupCode={lookupCode}
                                        amount={amount}
                                        isLoading={
                                            isLoading
                                        }
                                        onBack={
                                            handleBack
                                        }
                                        onExecute={
                                            handleExecute
                                        }
                                    />
                                )}

                                <AtmSlots />
                            </div>
                        </div>

                        {/* Footer */}
                        <div
                            className="
                                border-t
                                border-border
                                bg-surface
                                px-5
                                py-3
                                text-center
                                text-xs
                                text-muted
                            "
                        >
                            {step === "SUCCESS" &&
                            result
                                ? `${t("executionId")}: ${result.executionId}`
                                : t("secureAtm")}
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}