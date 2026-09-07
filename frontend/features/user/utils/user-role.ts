import type {CurrentUser} from "../types/user";

export function hasAdminAccess(
    currentUser: CurrentUser | null,
): boolean {
    return (
        currentUser?.roles.includes("STAFF") ||
        currentUser?.roles.includes("ADMIN") ||
        false
    );
}