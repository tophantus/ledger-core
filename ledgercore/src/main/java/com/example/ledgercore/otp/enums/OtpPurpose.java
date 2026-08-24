package com.example.ledgercore.otp.enums;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum OtpPurpose {

    EMAIL_VERIFICATION(Duration.ofMinutes(10)),
    RESET_PASSWORD(Duration.ofMinutes(5)),
    CONFIRM_TRANSFER(Duration.ofMinutes(3)),
    CHANGE_EMAIL(Duration.ofMinutes(5)),
    CHANGE_PHONE(Duration.ofMinutes(5)),
    TWO_FACTOR_AUTH(Duration.ofMinutes(3));

    private final Duration expiration;

    OtpPurpose(Duration expiration) {
        this.expiration = expiration;
    }
}