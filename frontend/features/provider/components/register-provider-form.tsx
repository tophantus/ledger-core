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

    const schema =
        createRegisterPaymentProviderSchema(
            t,
        );

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
                                text-text-primary outline-none
                                transition
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
                                text-text-primary outline-none
                                transition
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
                                text-text-primary outline-none
                                transition
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
                    <h2 className="text-lg font-semibold text-text-primary">
                        {t("success.title")}
                    </h2>

                    <p className="mt-1 text-sm text-text-secondary">
                        {t("success.description")}
                    </p>

                    <div className="mt-5 space-y-3">
                        <div>
                            <span className="text-sm text-text-secondary">
                                {t(
                                    "success.clientId",
                                )}
                            </span>

                            <p className="mt-1 font-mono text-sm text-text-primary">
                                {result.clientId}
                            </p>
                        </div>

                        <div>
                            <span className="text-sm text-text-secondary">
                                {t(
                                    "success.credential",
                                )}
                            </span>

                            <p className="mt-1 break-all font-mono text-sm text-text-primary">
                                {result.credential}
                            </p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}