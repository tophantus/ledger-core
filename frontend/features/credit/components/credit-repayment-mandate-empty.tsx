"use client";

import {useTranslations} from "next-intl";

interface CreditRepaymentMandateEmptyProps {
    onRegister: () => void;
}

export function CreditRepaymentMandateEmpty({
                                                onRegister,
                                            }: CreditRepaymentMandateEmptyProps) {
    const t = useTranslations("credit");

    return (
        <div className="mt-4">
            <p className="text-sm text-text-secondary">
                {t(
                    "facility.repaymentMandate.notConfigured",
                )}
            </p>

            <button
                type="button"
                onClick={onRegister}
                className="
                    mt-4
                    rounded-lg
                    bg-primary
                    px-4
                    py-2.5
                    text-sm
                    font-medium
                    text-primary-foreground
                    transition
                    hover:bg-primary/90
                "
            >
                {t(
                    "facility.repaymentMandate.actions.register",
                )}
            </button>
        </div>
    );
}