package com.example.ledgercore.account.adapter.outbound;

import com.example.ledgercore.account.command.port.outbound.AccountNumberGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGeneratorAdapter
        implements AccountNumberGeneratorPort {

    private static final int ACCOUNT_NUMBER_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder accountNumber = new StringBuilder(
                ACCOUNT_NUMBER_LENGTH
        );

        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            accountNumber.append(random.nextInt(10));
        }

        return accountNumber.toString();
    }
}