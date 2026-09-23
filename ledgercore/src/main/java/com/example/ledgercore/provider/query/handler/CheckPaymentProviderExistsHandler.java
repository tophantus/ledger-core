package com.example.ledgercore.provider.query.handler;

import com.example.ledgercore.provider.query.port.inbound.CheckPaymentProviderExistsUseCase;
import com.example.ledgercore.provider.query.repository.PaymentProviderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckPaymentProviderExistsHandler
        implements CheckPaymentProviderExistsUseCase {

    private final PaymentProviderQueryRepository
            paymentProviderQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean execute(UUID providerId) {
        if (providerId == null) {
            return false;
        }

        return paymentProviderQueryRepository.existsById(providerId);
    }
}