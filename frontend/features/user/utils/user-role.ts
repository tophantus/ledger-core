import type {CurrentUser} from "../types/user";
import {UserRole} from "@/features/user/types/role";

export function hasAdminAccess(
    currentUser: CurrentUser | null,
): boolean {
    return (
        currentUser?.roles.includes(UserRole.STAFF) ||
        currentUser?.roles.includes(UserRole.ADMIN) ||
        false
    );
}