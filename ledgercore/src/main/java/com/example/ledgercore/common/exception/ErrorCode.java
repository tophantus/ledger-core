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

    INVALID_OTP(
            "AUTH_003",
            "Invalid OTP",
            HttpStatus.BAD_REQUEST
    ),

    OTP_EXPIRED(
            "AUTH_004",
            "OTP has expired",
            HttpStatus.BAD_REQUEST
    ),

    OTP_ALREADY_VERIFIED(
            "AUTH_005",
            "OTP has already been verified",
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