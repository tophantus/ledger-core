"use client";

import {Plus} from "lucide-react";
import {useTranslations} from "next-intl";

interface CreateCardItemProps {
    onClick: () => void;
}

export function CreateCardItem({
                                   onClick,
                               }: CreateCardItemProps) {
    const t = useTranslations("card");

    return (
        <button
            type="button"
            onClick={onClick}
            className="
                group
                relative
                flex
                min-h-[260px]
                w-full
                flex-col
                items-center
                justify-center
                gap-4
                overflow-hidden
                rounded-2xl
                border
                border-dashed
                border-slate-400/30
                bg-gradient-to-br
                from-slate-700
                via-slate-650
                to-slate-600
                text-white/70
                shadow-lg
                transition
                hover:border-slate-300/50
                hover:shadow-xl
            "
        >
            <div
                className="
                    pointer-events-none
                    absolute
                    -right-12
                    -top-12
                    h-40
                    w-40
                    rounded-full
                    bg-white/10
                    transition
                    group-hover:bg-white/15
                "
            />

            <div
                className="
                    pointer-events-none
                    absolute
                    -bottom-20
                    -left-10
                    h-48
                    w-48
                    rounded-full
                    bg-white/5
                "
            />

            <div
                className="
                    relative
                    flex
                    size-14
                    items-center
                    justify-center
                    rounded-full
                    border
                    border-white/20
                    bg-white/10
                    transition
                    group-hover:bg-white/15
                "
            >
                <Plus
                    className="
                        size-7
                        text-white/80
                        transition
                        group-hover:text-white
                    "
                    strokeWidth={1.8}
                />
            </div>
        </button>
    );
}