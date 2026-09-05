import type {Locale} from "./config";

export async function getMessages(locale: Locale) {
    const [
        common,
        dashboard,
        auth,
    ] = await Promise.all([
        import(`../messages/${locale}/common.json`),
        import(`../messages/${locale}/dashboard.json`),
        import(`../messages/${locale}/auth.json`),
    ]);

    return {
        common: common.default,
        dashboard: dashboard.default,
        auth: auth.default,
    };
}