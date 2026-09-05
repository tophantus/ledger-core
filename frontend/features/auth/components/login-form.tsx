"use client";

import {useTranslations} from "next-intl";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";

import {useRouter, Link} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {useLogin} from "../hooks/use-login";
import {
    loginSchema,
    type LoginFormValues,
} from "../schemas/auth-schema";

export function LoginForm() {
    const router = useRouter();

    const t = useTranslations("auth.login");
    const tErrors = useTranslations("auth.errors");

    const {login} = useLogin();

    const {
        register,
        handleSubmit,
        formState: {
            errors,
            isSubmitting,
        },
        setError,
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (
        values: LoginFormValues,
    ) => {
        try {
            const result = await login(values);

            console.log(result)
            if (!result.success) {
                setError("root", {
                    message:
                        result.errorCode &&
                        tErrors.has(result.errorCode)
                            ? tErrors(result.errorCode)
                            : t("errors.generic"),
                });

                return;
            }

            if (result.data.status === "EMAIL_NOT_VERIFIED") {
                router.push(ROUTES.AUTH.VERIFY_EMAIL);
                return;
            }

            if (result.data.status === "AUTHENTICATED") {
                router.push(ROUTES.DASHBOARD,);
                return;
            }

            setError("root", {
                message: t("errors.generic"),
            });
        } catch {
            setError("root", {
                message: t("errors.generic"),
            });
        }
    };

    return (
        <form
            onSubmit={handleSubmit(onSubmit)}
            className="space-y-6"
        >
            <div className="space-y-1">
                <h1 className="text-2xl font-semibold text-text-primary">
                    {t("title")}
                </h1>

                <p className="text-sm text-text-secondary">
                    {t("description")}
                </p>
            </div>

            {errors.root && (
                <div className="rounded-md border border-danger/20 bg-danger-subtle px-4 py-3 text-sm text-danger">
                    {errors.root.message}
                </div>
            )}

            <div className="space-y-2">
                <label
                    htmlFor="email"
                    className="text-sm font-medium text-text-primary"
                >
                    {t("email")}
                </label>

                <input
                    id="email"
                    type="email"
                    autoComplete="email"
                    placeholder={t("emailPlaceholder")}
                    {...register("email")}
                    className="w-full rounded-md border bg-surface px-3 py-2.5 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                />

                {errors.email && (
                    <p className="text-sm text-danger">
                        {errors.email.message}
                    </p>
                )}
            </div>

            <div className="space-y-2">
                <label
                    htmlFor="password"
                    className="text-sm font-medium text-text-primary"
                >
                    {t("password")}
                </label>

                <input
                    id="password"
                    type="password"
                    autoComplete="current-password"
                    placeholder={t("passwordPlaceholder")}
                    {...register("password")}
                    className="w-full rounded-md border bg-surface px-3 py-2.5 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                />

                {errors.password && (
                    <p className="text-sm text-danger">
                        {errors.password.message}
                    </p>
                )}
            </div>

            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-md bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
            >
                {isSubmitting
                    ? t("submitting")
                    : t("submit")}
            </button>

            <div className="text-center text-sm text-text-secondary">
                {t("noAccount")}{" "}

                <Link
                    href={ROUTES.AUTH.REGISTER}
                    className="font-medium text-primary hover:underline"
                >
                    {t("signUp")}
                </Link>
            </div>
        </form>
    );
}