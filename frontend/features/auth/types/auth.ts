export interface SignUpRequest {
    fullName: string;
    email: string;
    password: string;
}

export interface VerifyEmailRequest {
    userId: string;
    otp: string;
}

export interface ResendVerificationCodeRequest {
    email: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface TokenResponse {
    accessToken: string;
    refreshToken: string;
}

export type LoginStatus =
    | "AUTHENTICATED"
    | "EMAIL_NOT_VERIFIED";

export interface LoginResponse {
    status: LoginStatus;
    token: TokenResponse | null;
    userId: string;
}

export interface SignUpResponse {
    userId: string;
    email: string;
    active: boolean;
}

export interface LogoutResponse {
    success: boolean;
}