import {useTranslations} from "next-intl";

export function ProviderAccountEmpty() {
    const t = useTranslations("provider.account");

    return (
        <div
            className="
                rounded-xl
                border
                border-border
                bg-surface
                p-8
                text-center
            "
        >
            <h3 className="text-sm font-semibold text-text-primary">
                {t("empty.title")}
            </h3>

            <p className="mt-1 text-sm text-text-secondary">
                {t("empty.description")}
            </p>
        </div>
    );
}