package com.example.ledgercore.outbox.event;

public enum OutboxAggregateType {

    OTP("OTP"),

    TRANSACTION("TRANSACTION"),

    BUSINESS_DAY("BUSINESS_DAY");

    private final String value;

    OutboxAggregateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}