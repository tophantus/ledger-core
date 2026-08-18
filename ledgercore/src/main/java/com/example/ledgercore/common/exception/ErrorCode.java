package com.example.ledgercore.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(
            "COMMON_001",
            "Invalid request",
            HttpStatus.BAD_REQUEST
    ),

    USER_NOT_FOUND(
            "USER_001",
            "User not found",
            HttpStatus.NOT_FOUND
    ),

    EMAIL_ALREADY_EXISTS(
            "USER_002",
            "Email already exists",
            HttpStatus.CONFLICT
    ),

    USER_ALREADY_ACTIVE(
            "USER_003",
            "User is already active",
            HttpStatus.CONFLICT
    ),

    INVALID_CREDENTIALS(
            "AUTH_001",
            "Invalid credentials",
            HttpStatus.UNAUTHORIZED
    ),

    USER_NOT_ACTIVE(
            "AUTH_002",
            "User is not active",
            HttpStatus.FORBIDDEN
    ),

    INVALID_VERIFICATION_CODE(
            "AUTH_003",
            "Invalid verification code",
            HttpStatus.BAD_REQUEST
    ),

    VERIFICATION_CODE_EXPIRED(
            "AUTH_004",
            "Verification code has expired",
            HttpStatus.BAD_REQUEST
    ),

    VERIFICATION_ALREADY_COMPLETED(
            "AUTH_005",
            "Email has already been verified",
            HttpStatus.BAD_REQUEST
    ),

    UNAUTHORIZED(
            "AUTH_006",
            "Authentication is required",
            HttpStatus.UNAUTHORIZED
    ),

    ACCESS_DENIED(
            "AUTH_007",
            "Access denied",
            HttpStatus.FORBIDDEN
    ),

    VERIFICATION_RATE_LIMITED(
            "AUTH_008",
            "Too many verification code requests",
            HttpStatus.TOO_MANY_REQUESTS
    ),

    VERIFICATION_CODE_COOLDOWN(
            "AUTH_009",
            "Please wait before requesting another verification code",
            HttpStatus.TOO_MANY_REQUESTS
    ),

    INVALID_CURRENT_PASSWORD(
            "AUTH_010",
            "Current password is incorrect",
            HttpStatus.BAD_REQUEST
    ),

    PASSWORD_SAME_AS_CURRENT(
            "AUTH_011",
            "New password must be different from current password",
            HttpStatus.BAD_REQUEST
    ),

    INTERNAL_ERROR(
            "SYSTEM_001",
            "Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    REQUEST_IN_PROGRESS(
            "SYSTEM_002",
            "Request is being processed",
            HttpStatus.CONFLICT
    );



    private final String code;

    private final String message;

    private final HttpStatus status;


    ErrorCode(
            String code,
            String message,
            HttpStatus status
    ) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}