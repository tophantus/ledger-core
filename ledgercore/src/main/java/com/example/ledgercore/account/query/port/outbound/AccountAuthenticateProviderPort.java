package com.example.ledgercore.account.query.port.outbound;

import java.util.UUID;

public interface AccountAuthenticateProviderPort {
    UUID authenticate(
            String clientId,
            String credential
    );
}
