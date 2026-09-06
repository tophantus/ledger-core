import type {Locale} from "./config";

export async function getMessages(locale: Locale) {
    const [
        common,
        dashboard,
        auth,
        account,
        transaction,
    ] = await Promise.all([
        import(`../messages/${locale}/common.json`),
        import(`../messages/${locale}/dashboard.json`),
        import(`../messages/${locale}/auth.json`),
        import(`../messages/${locale}/account.json`),
        import(`../messages/${locale}/transaction.json`),
    ]);

    return {
        common: common.default,
        dashboard: dashboard.default,
        auth: auth.default,
        account: account.default,
        transaction: transaction.default
    };
}