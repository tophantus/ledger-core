import type {UserRole} from "./role";

export type UserStatus =
    | "PENDING_VERIFICATION"
    | "ACTIVE"
    | "BLOCKED"
    | "CLOSED";

export interface UserProfile {
    fullName: string;
    avatarUrl: string | null;
}

export interface CurrentUser {
    id: string;
    email: string;
    roles: UserRole[];
    status: UserStatus;
    profile: UserProfile;
    createdAt: string;
}