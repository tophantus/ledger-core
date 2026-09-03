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

    USER_PROFILE_NOT_FOUND(
            "USER_004",
            "User profile not found",
            HttpStatus.NOT_FOUND
    ),

    ROLE_NOT_FOUND(
            "ROLE_001",
            "Role not found",
            HttpStatus.NOT_FOUND
    ),

    OTP_INVALID(
            "OTP_001",
            "Invalid OTP",
            HttpStatus.BAD_REQUEST
    ),

    OTP_EXPIRED(
            "OTP_002",
            "OTP has expired",
            HttpStatus.BAD_REQUEST
    ),

    OTP_LOCKED(
            "OTP_003",
            "OTP verification is locked",
            HttpStatus.CONFLICT
    ),

    ACCOUNT_NOT_FOUND(
            "ACCOUNT_001",
            "Account not found",
            HttpStatus.NOT_FOUND
    ),

    ACCOUNT_ALREADY_ACTIVE(
            "ACCOUNT_002",
            "Account is already active",
            HttpStatus.CONFLICT
    ),

    ACCOUNT_ALREADY_BLOCKED(
            "ACCOUNT_003",
            "Account is already blocked",
            HttpStatus.CONFLICT
    ),

    ACCOUNT_ALREADY_CLOSED(
            "ACCOUNT_004",
            "Account is already closed",
            HttpStatus.CONFLICT
    ),

    ACCOUNT_BALANCE_NOT_ZERO(
            "ACCOUNT_005",
            "Account balance must be zero before closing",
            HttpStatus.CONFLICT
    ),


    ACCOUNT_INSUFFICIENT_BALANCE(
            "ACCOUNT_006",
            "Account has insufficient balance",
            HttpStatus.CONFLICT
    ),

    ACCOUNT_NOT_ACTIVE(
            "ACCOUNT_007",
            "Account is not active",
            HttpStatus.CONFLICT
    ),

    BUSINESS_DAY_NOT_FOUND(
            "BUSINESS_DAY_001",
            "Current business day not found",
            HttpStatus.NOT_FOUND
    ),

    // Ledger

    LEDGER_ACCOUNT_NOT_FOUND(
            "LEDGER_001",
            "Ledger account not found",
            HttpStatus.NOT_FOUND
    ),

    LEDGER_ACCOUNT_NOT_ACTIVE(
            "LEDGER_002",
            "Ledger account is not active",
            HttpStatus.CONFLICT
    ),

    // Transaction

    TRANSACTION_NOT_FOUND(
            "TRANSACTION_001",
            "Transaction not found",
            HttpStatus.NOT_FOUND
    ),

    TRANSACTION_REFERENCE_ALREADY_EXISTS(
            "TRANSACTION_002",
            "Transaction reference already exists",
            HttpStatus.CONFLICT
    ),

    SAME_ACCOUNT_TRANSFER(
            "TRANSACTION_003",
            "Source and destination accounts must be different",
            HttpStatus.BAD_REQUEST
    ),

    TRANSACTION_CURRENCY_MISMATCH(
            "TRANSACTION_004",
            "Source and destination account currencies must match",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_TRANSFER_AMOUNT(
            "TRANSACTION_005",
            "Transfer amount must be greater than zero",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_TRANSACTION_STATUS(
            "TRANSACTION_006",
            "Transaction is not in a valid state for this operation",
            HttpStatus.CONFLICT
    ),

    TRANSACTION_ALREADY_COMPLETED(
            "TRANSACTION_007",
            "Transaction has already been completed",
            HttpStatus.CONFLICT
    ),

    TRANSACTION_ALREADY_FAILED(
            "TRANSACTION_008",
            "Transaction has already failed",
            HttpStatus.CONFLICT
    ),

    INVALID_DEPOSIT_AMOUNT(
            "TRANSACTION_009",
            "Deposit amount must be greater than zero",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_WITHDRAW_AMOUNT(
            "TRANSACTION_010",
            "Withdraw amount must be greater than zero",
            HttpStatus.BAD_REQUEST
    ),

    TRANSFER_INTENT_NOT_FOUND(
            "TRANSACTION_011",
            "Transfer intent not found",
            HttpStatus.NOT_FOUND
    ),

    TRANSFER_INTENT_EXPIRED(
            "TRANSACTION_012",
            "Transfer intent has expired",
            HttpStatus.CONFLICT
    ),

    INVALID_CREDENTIALS(
            "AUTH_001",
            "Invalid credentials",
            HttpStatus.UNAUTHORIZED
    ),

    // Webhook

    INVALID_WEBHOOK_URL(
            "WEBHOOK_001",
            "Invalid webhook URL",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_WEBHOOK_EVENT_TYPES(
            "WEBHOOK_002",
            "Webhook event types must not be empty",
            HttpStatus.BAD_REQUEST
    ),

    WEBHOOK_ALREADY_EXISTS(
            "WEBHOOK_003",
            "Webhook endpoint already exists",
            HttpStatus.CONFLICT
    ),

    WEBHOOK_NOT_FOUND(
            "WEBHOOK_004",
            "Webhook endpoint not found",
            HttpStatus.NOT_FOUND
    ),

    WEBHOOK_DISABLED(
            "WEBHOOK_005",
            "Webhook endpoint is disabled",
            HttpStatus.CONFLICT
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

    INVALID_REFRESH_TOKEN(
            "AUTH_008",
            "Invalid refresh token",
            HttpStatus.UNAUTHORIZED
    ),

    REFRESH_TOKEN_REVOKED(
            "AUTH_009",
            "Refresh token has been revoked",
            HttpStatus.UNAUTHORIZED
    ),

    REFRESH_TOKEN_EXPIRED(
            "AUTH_010",
            "Refresh token has expired",
            HttpStatus.UNAUTHORIZED
    ),

    VERIFICATION_RATE_LIMITED(
            "AUTH_011",
            "Too many verification code requests",
            HttpStatus.TOO_MANY_REQUESTS
    ),

    VERIFICATION_CODE_COOLDOWN(
            "AUTH_012",
            "Please wait before requesting another verification code",
            HttpStatus.TOO_MANY_REQUESTS
    ),

    INVALID_CURRENT_PASSWORD(
            "AUTH_013",
            "Current password is incorrect",
            HttpStatus.BAD_REQUEST
    ),

    PASSWORD_SAME_AS_CURRENT(
            "AUTH_014",
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