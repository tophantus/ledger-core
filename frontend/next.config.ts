import type {NextConfig} from "next";
import createNextIntlPlugin from "next-intl/plugin";

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                source: "/api/backend/:path*",
                destination: `${process.env.API_BASE_URL}/:path*`,
            },
        ];
    },
};

const withNextIntl = createNextIntlPlugin("./i18n/request.ts");

export default withNextIntl(nextConfig);