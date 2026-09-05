"use client";

import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";

import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";

import {useVerifyEmail} from "../hooks/use-verify-email";
import {useResendVerificationCode} from "../hooks/use-resend-verification-code";
import {
    verifyEmailSchema,
    type VerifyEmailFormValues,
} from "../schemas/auth-schema";
import {useAuthStore} from "../stores/auth-store";

const RESEND_COOLDOWN_SECONDS = 30;

export function VerifyEmailForm() {
    const router = useRouter();

    const t = useTranslations("auth.verifyEmail");
    const tErrors = useTranslations("auth.errors");

    const {verifyEmail} = useVerifyEmail();
    const {resendVerificationCode} =
        useResendVerificationCode();

    const pendingVerification = useAuthStore(
        (state) => state.pendingVerification,
    );

    const [resendCooldown, setResendCooldown] =
        useState(0);

    const [isResending, setIsResending] =
        useState(false);

    const {
        register,
        handleSubmit,
        formState: {errors, isSubmitting},
        setError,
    } = useForm<VerifyEmailFormValues>({
        resolver: zodResolver(verifyEmailSchema),
    });

    useEffect(() => {
        if (!pendingVerification) {
            router.replace(ROUTES.AUTH.LOGIN);
        }
    }, [pendingVerification, router]);

    useEffect(() => {
        if (resendCooldown <= 0) {
            return;
        }

        const timer = window.setInterval(() => {
            setResendCooldown((current) =>
                current <= 1 ? 0 : current - 1,
            );
        }, 1000);

        return () => {
            window.clearInterval(timer);
        };
    }, [resendCooldown]);

    if (!pendingVerification) {
        return null;
    }

    const onSubmit = async (
        values: VerifyEmailFormValues,
    ) => {
        try {
            const result = await verifyEmail({
                userId: pendingVerification.userId,
                otp: values.otp,
            });

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

            router.replace(
                ROUTES.DASHBOARD,
            );
        } catch {
            setError("root", {
                message: t("errors.generic"),
            });
        }
    };

    const handleResend = async () => {
        if (
            isResending ||
            resendCooldown > 0
        ) {
            return;
        }

        setIsResending(true);

        try {
            const result =
                await resendVerificationCode({
                    email: pendingVerification.email,
                });

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

            setResendCooldown(
                RESEND_COOLDOWN_SECONDS,
            );
        } catch {
            setError("root", {
                message: t("errors.generic"),
            });
        } finally {
            setIsResending(false);
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

                <p className="pt-2 text-sm font-medium text-text-primary">
                    {pendingVerification.email}
                </p>
            </div>

            {errors.root && (
                <div className="rounded-md border border-danger/20 bg-danger-subtle px-4 py-3 text-sm text-danger">
                    {errors.root.message}
                </div>
            )}

            <div className="space-y-2">
                <label
                    htmlFor="otp"
                    className="text-sm font-medium text-text-primary"
                >
                    {t("otp")}
                </label>

                <input
                    id="otp"
                    type="text"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    maxLength={6}
                    placeholder={t("otpPlaceholder")}
                    {...register("otp")}
                    className="w-full rounded-md border bg-surface px-3 py-2.5 text-center text-lg tracking-[0.4em] outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                />

                {errors.otp && (
                    <p className="text-sm text-danger">
                        {errors.otp.message}
                    </p>
                )}
            </div>

            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-md bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
            >
                {isSubmitting
                    ? t("verifying")
                    : t("submit")}
            </button>

            <div className="text-center">
                <button
                    type="button"
                    onClick={handleResend}
                    disabled={
                        isResending ||
                        resendCooldown > 0
                    }
                    className="text-sm font-medium text-primary hover:underline disabled:cursor-not-allowed disabled:no-underline disabled:opacity-50"
                >
                    {isResending
                        ? t("resending")
                        : resendCooldown > 0
                            ? t("resendIn", {
                                seconds:
                                resendCooldown,
                            })
                            : t("resend")}
                </button>
            </div>
        </form>
    );
}