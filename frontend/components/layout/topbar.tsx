"use client";

import {Logo} from "@/components/common/logo";

import {UserProfileMenu} from "./user-profile-menu";

export function Topbar() {
    return (
        <header className="border-b border-border bg-surface">
            <div className="mx-auto flex h-16 items-center justify-between px-2 md:px-6">
                <Logo size={36} />

                <UserProfileMenu />
            </div>
        </header>
    );
}
