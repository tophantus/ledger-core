package com.example.ledgercore.provider.query.port.inbound;

import java.util.UUID;

public interface CheckPaymentProviderExistsUseCase {

    boolean execute(UUID providerId);
}