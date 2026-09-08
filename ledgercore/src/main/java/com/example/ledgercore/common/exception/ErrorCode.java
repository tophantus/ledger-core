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

    // User

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

    // Role

    ROLE_NOT_FOUND(
            "ROLE_001",
            "Role not found",
            HttpStatus.NOT_FOUND
    ),

    // OTP

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

    // Account

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

    ACCOUNT_CURRENCY_MISMATCH(
            "ACCOUNT_008",
            "Account currency mismatch",
            HttpStatus.BAD_REQUEST
    ),

    // Account Daily Balance

    ACCOUNT_DAILY_BALANCE_NOT_FOUND(
            "ACCOUNT_010",
            "Account daily balance not found",
            HttpStatus.NOT_FOUND
    ),

    // Product

    PRODUCT_NOT_FOUND(
            "PRODUCT_001",
            "Product not found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_NOT_ACTIVE(
            "PRODUCT_002",
            "Product is not active",
            HttpStatus.CONFLICT
    ),

    // Business Day

    BUSINESS_DAY_NOT_FOUND(
            "BUSINESS_DAY_001",
            "Current business day not found",
            HttpStatus.NOT_FOUND
    ),

    INVALID_BUSINESS_DAY_STATUS(
            "BUSINESS_DAY_002",
            "Invalid business day status",
            HttpStatus.CONFLICT
    ),

    BUSINESS_DAY_CLOSE_NOT_ALLOWED(
            "BUSINESS_DAY_003",
            "Business day can only be closed during the closing window",
            HttpStatus.CONFLICT
    ),

    BUSINESS_DAY_DATE_MISMATCH(
            "BUSINESS_DAY_004",
            "Current business day does not match system date",
            HttpStatus.CONFLICT
    ),

    NEXT_BUSINESS_DAY_ALREADY_EXISTS(
            "BUSINESS_DAY_005",
            "Next business day already exists",
            HttpStatus.CONFLICT
    ),

    BUSINESS_DAY_NEXT_NOT_FOUND(
            "BUSINESS_DAY_008",
            "Next business day not found",
            HttpStatus.NOT_FOUND
    ),

    // Interest

    INTEREST_CONFIG_NOT_FOUND(
            "INTEREST_001",
            "Applicable interest configuration not found",
            HttpStatus.NOT_FOUND
    ),

    INTEREST_RUN_NOT_FOUND(
            "INTEREST_002",
            "Interest run not found",
            HttpStatus.NOT_FOUND
    ),

    INVALID_INTEREST_RUN_STATUS(
            "INTEREST_003",
            "Invalid interest run status",
            HttpStatus.CONFLICT
    ),

    INTEREST_RUN_ALREADY_COMPLETED(
            "INTEREST_004",
            "Interest run already completed",
            HttpStatus.CONFLICT
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

    // Withdrawal

    WITHDRAWAL_REQUEST_NOT_FOUND(
            "WITHDRAWAL_001",
            "Withdrawal request not found",
            HttpStatus.NOT_FOUND
    ),

    WITHDRAWAL_REQUEST_NOT_PENDING(
            "WITHDRAWAL_002",
            "Withdrawal request is not pending",
            HttpStatus.BAD_REQUEST
    ),

    WITHDRAWAL_REQUEST_EXPIRED(
            "WITHDRAWAL_003",
            "Withdrawal request has expired",
            HttpStatus.BAD_REQUEST
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

    // Authentication and Authorization

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

    // Reconciliation

    RECONCILIATION_PROCESSOR_NOT_FOUND(
            "RECONCILIATION_001",
            "Reconciliation processor not found",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    RECONCILIATION_RUN_NOT_FOUND(
            "RECONCILIATION_002",
            "Reconciliation run not found",
            HttpStatus.NOT_FOUND
    ),

    // ATM Terminal

    ATM_TERMINAL_ALREADY_EXISTS(
            "ATM_001",
            "ATM terminal already exists",
            HttpStatus.CONFLICT
    ),

    ATM_TERMINAL_NOT_FOUND(
            "ATM_002",
            "ATM terminal not found",
            HttpStatus.NOT_FOUND
    ),

    ATM_TERMINAL_ALREADY_ACTIVE(
            "ATM_003",
            "ATM terminal is already active",
            HttpStatus.CONFLICT
    ),

    ATM_TERMINAL_ALREADY_INACTIVE(
            "ATM_004",
            "ATM terminal is already inactive",
            HttpStatus.CONFLICT
    ),

    // System

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