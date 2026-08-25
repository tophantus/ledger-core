package com.example.ledgercore.outbox.event;

public enum OutboxAggregateType {

    OTP("OTP"),

    TRANSACTION("TRANSACTION");

    private final String value;

    OutboxAggregateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}