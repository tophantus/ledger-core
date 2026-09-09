package com.example.ledgercore.withdrawal.command.port.outbound;

import java.util.UUID;

public interface AtmAuthenticationPort {

    UUID authenticate(
            String terminalCode,
            String credential
    );
}