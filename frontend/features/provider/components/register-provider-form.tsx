"use client";

import {useState} from "react";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useTranslations} from "next-intl";

import {useRegisterPaymentProvider} from "../hooks/use-register-payment-provider";
import {
    createRegisterPaymentProviderSchema,
    type RegisterPaymentProviderFormValues,
} from "../schemas/payment-provider-schema";
import {
    ProviderType,
    type RegisterPaymentProviderResult,
} from "../types/payment-provider";

export function RegisterProviderForm() {
    const t = useTranslations("provider.register");

    const {
        registerPaymentProvider,
    } = useRegisterPaymentProvider();

    const [result, setResult] =
        useState<RegisterPaymentProviderResult | null>(
            null,
        );

    const [copied, setCopied] =
        useState<string | null>(null);

    const schema =
        createRegisterPaymentProviderSchema(t);

    const {
        register,
        handleSubmit,
        reset,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<RegisterPaymentProviderFormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            code: "",
            name: "",
            type: ProviderType.PSP,
        },
    });

    const onSubmit = async (
        values: RegisterPaymentProviderFormValues,
    ) => {
        const response =
            await registerPaymentProvider(values);

        if (!response.success) {
            return;
        }

        setResult(response.data);
        reset();
    };

    const getEnvValue = (
        key: string,
        value: string,
    ) => `${key}=${value}`;

    const copyToClipboard = async (
        key: string,
        value: string,
    ) => {
        await navigator.clipboard.writeText(
            getEnvValue(key, value),
        );

        setCopied(key);

        window.setTimeout(() => {
            setCopied(null);
        }, 1500);
    };

    const copyEnvConfig = async (
        provider: RegisterPaymentProviderResult,
    ) => {
        const env = [
            `NEXT_PUBLIC_PROVIDER_NAME=${provider.name}`,
            `NEXT_PUBLIC_PROVIDER_CLIENT_ID=${provider.clientId}`,
            `NEXT_PUBLIC_PROVIDER_CREDENTIAL=${provider.credential}`,
        ].join("\n");

        await navigator.clipboard.writeText(env);

        setCopied("env");

        window.setTimeout(() => {
            setCopied(null);
        }, 1500);
    };

    return (
        <div className="w-full space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-text-primary">
                    {t("title")}
                </h1>

                <p className="mt-1 text-sm text-text-secondary">
                    {t("description")}
                </p>
            </div>

            <form
                onSubmit={handleSubmit(onSubmit)}
                className="
                    w-full rounded-xl border border-border
                    bg-surface p-6 shadow-sm
                "
            >
                <div className="space-y-5">
                    <div>
                        <label
                            htmlFor="provider-code"
                            className="block text-sm font-medium text-text-primary"
                        >
                            {t("fields.code.label")}
                        </label>

                        <input
                            id="provider-code"
                            type="text"
                            {...register("code")}
                            disabled={isSubmitting}
                            placeholder={t(
                                "fields.code.placeholder",
                            )}
                            className="
                                mt-2 w-full rounded-lg border border-border
                                bg-background px-3 py-2.5 text-sm
                                text-text-primary outline-none transition
                                focus:ring-2 focus:ring-primary/20
                                disabled:cursor-not-allowed
                                disabled:opacity-60
                            "
                        />

                        {errors.code && (
                            <p className="mt-1.5 text-sm text-danger">
                                {errors.code.message}
                            </p>
                        )}
                    </div>

                    <div>
                        <label
                            htmlFor="provider-name"
                            className="block text-sm font-medium text-text-primary"
                        >
                            {t("fields.name.label")}
                        </label>

                        <input
                            id="provider-name"
                            type="text"
                            {...register("name")}
                            disabled={isSubmitting}
                            placeholder={t(
                                "fields.name.placeholder",
                            )}
                            className="
                                mt-2 w-full rounded-lg border border-border
                                bg-background px-3 py-2.5 text-sm
                                text-text-primary outline-none transition
                                focus:ring-2 focus:ring-primary/20
                                disabled:cursor-not-allowed
                                disabled:opacity-60
                            "
                        />

                        {errors.name && (
                            <p className="mt-1.5 text-sm text-danger">
                                {errors.name.message}
                            </p>
                        )}
                    </div>

                    <div>
                        <label
                            htmlFor="provider-type"
                            className="block text-sm font-medium text-text-primary"
                        >
                            {t("fields.type.label")}
                        </label>

                        <select
                            id="provider-type"
                            {...register("type")}
                            disabled={isSubmitting}
                            className="
                                mt-2 w-full rounded-lg border border-border
                                bg-background px-3 py-2.5 text-sm
                                text-text-primary outline-none transition
                                focus:ring-2 focus:ring-primary/20
                                disabled:cursor-not-allowed
                                disabled:opacity-60
                            "
                        >
                            <option value={ProviderType.PSP}>
                                {t(
                                    "fields.type.options.psp",
                                )}
                            </option>

                            <option value={ProviderType.ACQUIRER}>
                                {t(
                                    "fields.type.options.acquirer",
                                )}
                            </option>
                        </select>
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="
                            w-full rounded-lg bg-primary
                            px-4 py-2.5 text-sm font-medium
                            text-white transition
                            hover:bg-primary/90
                            disabled:cursor-not-allowed
                            disabled:opacity-60
                        "
                    >
                        {isSubmitting
                            ? t("actions.registering")
                            : t("actions.register")}
                    </button>
                </div>
            </form>

            {result && (
                <div
                    className="
                        w-full rounded-xl border border-border
                        bg-surface p-6 shadow-sm
                    "
                >
                    <div className="flex items-start justify-between gap-4">
                        <div>
                            <h2 className="text-lg font-semibold text-text-primary">
                                {t("success.title")}
                            </h2>

                            <p className="mt-1 text-sm text-text-secondary">
                                {t("success.description")}
                            </p>
                        </div>

                        <button
                            type="button"
                            onClick={() =>
                                copyEnvConfig(result)
                            }
                            className="
                                shrink-0 rounded-lg border border-border
                                px-3 py-2 text-xs font-medium
                                text-text-secondary transition
                                hover:bg-surface-subtle
                            "
                        >
                            {copied === "env"
                                ? t("success.copied")
                                : t("success.copyEnv")}
                        </button>
                    </div>

                    <div className="mt-5 space-y-3">
                        <div>
                            <div className="flex items-center justify-between gap-3">
                                <span className="text-sm text-text-secondary">
                                    {t("success.providerName")}
                                </span>

                                <button
                                    type="button"
                                    onClick={() =>
                                        copyToClipboard(
                                            "NEXT_PUBLIC_PROVIDER_NAME",
                                            result.name,
                                        )
                                    }
                                    className="
                                        text-xs font-medium
                                        text-primary hover:underline
                                    "
                                >
                                    {copied ===
                                    "NEXT_PUBLIC_PROVIDER_NAME"
                                        ? t("success.copied")
                                        : t("success.copy")}
                                </button>
                            </div>

                            <p className="mt-1 break-all font-mono text-sm text-text-primary">
                                NEXT_PUBLIC_PROVIDER_NAME=
                                {result.name}
                            </p>
                        </div>

                        <div>
                            <div className="flex items-center justify-between gap-3">
                                <span className="text-sm text-text-secondary">
                                    {t("success.clientId")}
                                </span>

                                <button
                                    type="button"
                                    onClick={() =>
                                        copyToClipboard(
                                            "NEXT_PUBLIC_PROVIDER_CLIENT_ID",
                                            result.clientId,
                                        )
                                    }
                                    className="
                                        text-xs font-medium
                                        text-primary hover:underline
                                    "
                                >
                                    {copied ===
                                    "NEXT_PUBLIC_PROVIDER_CLIENT_ID"
                                        ? t("success.copied")
                                        : t("success.copy")}
                                </button>
                            </div>

                            <p className="mt-1 break-all font-mono text-sm text-text-primary">
                                NEXT_PUBLIC_PROVIDER_CLIENT_ID=
                                {result.clientId}
                            </p>
                        </div>

                        <div>
                            <div className="flex items-center justify-between gap-3">
                                <span className="text-sm text-text-secondary">
                                    {t("success.credential")}
                                </span>

                                <button
                                    type="button"
                                    onClick={() =>
                                        copyToClipboard(
                                            "NEXT_PUBLIC_PROVIDER_CREDENTIAL",
                                            result.credential,
                                        )
                                    }
                                    className="
                                        text-xs font-medium
                                        text-primary hover:underline
                                    "
                                >
                                    {copied ===
                                    "NEXT_PUBLIC_PROVIDER_CREDENTIAL"
                                        ? t("success.copied")
                                        : t("success.copy")}
                                </button>
                            </div>

                            <p className="mt-1 break-all font-mono text-sm text-text-primary">
                                NEXT_PUBLIC_PROVIDER_CREDENTIAL=
                                {result.credential}
                            </p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}