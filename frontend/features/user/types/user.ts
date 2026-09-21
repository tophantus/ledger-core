import type {UserRole} from "./role";

export enum UserStatus {
    PENDING_VERIFICATION = "PENDING_VERIFICATION",
    ACTIVE = "ACTIVE",
    BLOCKED = "BLOCKED",
    CLOSED = "CLOSED",
}

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