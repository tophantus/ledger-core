import {ArrowRight} from "lucide-react";
import Link from "next/link";

interface InterestOverviewLinkProps {
    href: string;
    title: string;
    description: string;
}

export function InterestOverviewLink({
                                         href,
                                         title,
                                         description,
                                     }: InterestOverviewLinkProps) {
    return (
        <Link
            href={href}
            className="
                group
                rounded-lg
                border
                border-border
                bg-surface
                p-5
                transition
                hover:bg-background-subtle
            "
        >
            <div className="
                flex
                items-center
                justify-between
            ">
                <h3 className="
                    font-medium
                    text-foreground
                ">
                    {title}
                </h3>

                <ArrowRight className="
                    h-4
                    w-4
                    text-muted
                    transition
                    group-hover:translate-x-1
                " />
            </div>

            <p className="
                mt-2
                text-sm
                text-muted
            ">
                {description}
            </p>
        </Link>
    );
}