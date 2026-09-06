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
    status: UserStatus;
    profile: UserProfile;
    createdAt: string;
}