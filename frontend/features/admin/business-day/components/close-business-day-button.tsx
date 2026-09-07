import {useState} from "react";
import {useTranslations} from "next-intl";

import {Button} from "@/components/ui/button";
import {useAdminBusinessDay} from "../hooks/use-admin-business-day";

export function CloseBusinessDayButton() {
    const t = useTranslations(
        "admin.businessDay",
    );
    const tErrors = useTranslations("errors");

    const {closeBusinessDay} =
        useAdminBusinessDay();

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const handleClose = async () => {
        if (loading) {
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const response =
                await closeBusinessDay();

            if (!response.success) {
                setError(
                    response.code &&
                    tErrors.has(response.code)
                        ? tErrors(
                            response.code,
                        )
                        : tErrors("fallback"),
                );
                return;
            }

            // success
        } catch {
            setError(
                tErrors("fallback"),
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-2">
            <Button
                type="button"
                variant="danger"
                loading={loading}
                onClick={handleClose}
            >
                {t("close")}
            </Button>

            {error && (
                <p className="text-sm text-danger">
                    {error}
                </p>
            )}
        </div>
    );
}