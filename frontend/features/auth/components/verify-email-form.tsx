"use client";

import {useEffect, useState} from "react";
import {useTranslations} from "next-intl";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";

import {useRouter} from "@/i18n/routing";
import {ROUTES} from "@/lib/constants/routes";
import {Button} from "@/components/ui/button";

import {useVerifyEmail} from "../hooks/use-verify-email";
import {useResendVerificationCode} from "../hooks/use-resend-verification-code";
import {
    verifyEmailSchema,
    type VerifyEmailFormValues,
} from "../schemas/auth-schema";
import {useAuthStore} from "../stores/auth-store";
import {Logo} from "@/components/common/logo";

const RESEND_COOLDOWN_SECONDS = 30;

export function VerifyEmailForm() {
    const router = useRouter();

    const t = useTranslations("auth.verifyEmail");
    const tErrors = useTranslations("errors");

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
                        result.code &&
                        tErrors.has(result.code)
                            ? tErrors(result.code)
                            : tErrors("fallback"),
                });

                return;
            }

            router.replace(ROUTES.DASHBOARD);
        } catch {
            setError("root", {
                message: tErrors("fallback"),
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
                        result.code &&
                        tErrors.has(result.code)
                            ? tErrors(result.code)
                            : tErrors("fallback"),
                });

                return;
            }

            setResendCooldown(
                RESEND_COOLDOWN_SECONDS,
            );
        } catch {
            setError("root", {
                message: tErrors("fallback"),
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
            <div className="flex flex-col items-center gap-4">
                <Logo size={56} />

                <div className="space-y-1 text-center">
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

            <Button
                type="submit"
                loading={isSubmitting}
                className="w-full"
            >
                {t("submit")}
            </Button>

            <div className="text-center">
                <Button
                    type="button"
                    variant="ghost"
                    loading={isResending}
                    disabled={resendCooldown > 0}
                    onClick={handleResend}
                    className="text-sm font-medium text-primary hover:underline disabled:no-underline"
                >
                    {resendCooldown > 0
                        ? t("resendIn", {
                            seconds: resendCooldown,
                        })
                        : t("resend")}
                </Button>
            </div>
        </form>
    );
}