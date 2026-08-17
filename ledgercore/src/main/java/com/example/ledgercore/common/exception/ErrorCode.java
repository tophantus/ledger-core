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