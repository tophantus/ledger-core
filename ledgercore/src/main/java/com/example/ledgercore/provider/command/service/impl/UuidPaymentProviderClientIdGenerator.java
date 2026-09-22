package com.example.ledgercore.provider.command.service.impl;

import com.example.ledgercore.provider.command.service.PaymentProviderClientIdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidPaymentProviderClientIdGenerator
        implements PaymentProviderClientIdGenerator {

    @Override
    public String generate() {
        return "pc_" + UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}