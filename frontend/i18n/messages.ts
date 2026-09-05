import type { Locale } from "./config";

export async function getMessages(locale: Locale) {
    const [
        common,
        home
    ] = await Promise.all([
        import(`../messages/${locale}/common.json`),
        import(`../messages/${locale}/home.json`),
    ]);

    return {
        common: common.default,
        home: home.default
    };
}