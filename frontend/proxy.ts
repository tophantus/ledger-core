import createMiddleware from "next-intl/middleware";
import {NextRequest, NextResponse} from "next/server";

import {routing} from "./i18n/routing";

const intlMiddleware = createMiddleware(routing);

const PUBLIC_ROUTES = [
    "/login",
    "/register",
    "/verify-email",
    "/forgot-password",
];

const REFRESH_TOKEN_COOKIE = "refresh_token";
const DASHBOARD_ROUTE = "/dashboard";

export default function proxy(request: NextRequest) {
    const {pathname} = request.nextUrl;

    const pathnameWithoutLocale =
        pathname.replace(/^\/(vi|en)(?=\/|$)/, "") || "/";

    const isPublicRoute = PUBLIC_ROUTES.some(
        (route) =>
            pathnameWithoutLocale === route ||
            pathnameWithoutLocale.startsWith(`${route}/`),
    );

    const hasRefreshToken = request.cookies.has(
        REFRESH_TOKEN_COOKIE,
    );

    /*
     * Authenticated user must not access auth pages.
     */
    if (hasRefreshToken && isPublicRoute) {
        const url = request.nextUrl.clone();

        const locale = pathname.match(
            /^\/(vi|en)(?=\/|$)/,
        )?.[1];

        url.pathname = locale
            ? `/${locale}${DASHBOARD_ROUTE}`
            : DASHBOARD_ROUTE;

        return NextResponse.redirect(url);
    }

    /*
     * Unauthenticated user must not access private pages.
     */
    if (!hasRefreshToken && !isPublicRoute) {
        const url = request.nextUrl.clone();

        const locale = pathname.match(
            /^\/(vi|en)(?=\/|$)/,
        )?.[1];

        url.pathname = locale
            ? `/${locale}/login`
            : "/login";

        return NextResponse.redirect(url);
    }

    return intlMiddleware(request);
}

export const config = {
    matcher: [
        "/",
        "/(vi|en)/:path*",
        "/((?!api|_next|_vercel|.*\\..*).*)",
    ],
};