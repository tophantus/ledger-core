export function isProviderConfigured(): boolean {
    return Boolean(
        process.env.NEXT_PUBLIC_PROVIDER_NAME &&
        process.env.NEXT_PUBLIC_PROVIDER_CLIENT_ID &&
        process.env.NEXT_PUBLIC_PROVIDER_CREDENTIAL,
    );
}