package com.example.ledgercore.account.adapter.outbound.provider;

import com.example.ledgercore.account.command.port.outbound.AccountProviderPort;
import com.example.ledgercore.provider.query.port.inbound.CheckPaymentProviderExistsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountProviderAdapter
        implements AccountProviderPort {

    private final CheckPaymentProviderExistsUseCase
            checkPaymentProviderExistsUseCase;

    @Override
    public boolean existsById(UUID providerId) {
        return checkPaymentProviderExistsUseCase.execute(providerId);
    }
}