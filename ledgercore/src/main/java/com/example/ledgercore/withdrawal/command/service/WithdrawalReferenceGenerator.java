package com.example.ledgercore.withdrawal.command.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WithdrawalReferenceGenerator {

    public String generate() {
        return "WD-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}