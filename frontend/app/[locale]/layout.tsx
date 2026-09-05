import {NextIntlClientProvider} from "next-intl";
import {getLocale, getMessages} from "next-intl/server";

import type {ReactNode} from "react";

interface LocaleLayoutProps {
    children: ReactNode;
}

export default async function LocaleLayout({
                                               children,
                                           }: LocaleLayoutProps) {
    const locale = await getLocale();
    const messages = await getMessages();

    return (
        <NextIntlClientProvider
            locale={locale}
            messages={messages}
        >
            {children}
        </NextIntlClientProvider>
    );
}