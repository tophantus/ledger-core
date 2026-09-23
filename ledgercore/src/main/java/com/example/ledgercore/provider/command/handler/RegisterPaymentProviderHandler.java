package com.example.ledgercore.provider.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderResult;
import com.example.ledgercore.provider.command.port.inbound.RegisterPaymentProviderUseCase;
import com.example.ledgercore.provider.command.port.outbound.ProviderAccountPort;
import com.example.ledgercore.provider.command.repository.PaymentProviderCommandRepository;
import com.example.ledgercore.provider.entity.PaymentProvider;
import com.example.ledgercore.provider.enums.ProviderStatus;
import com.example.ledgercore.provider.command.service.PaymentProviderClientIdGenerator;
import com.example.ledgercore.provider.command.service.PaymentProviderCredentialGenerator;
import com.example.ledgercore.provider.command.service.PaymentProviderCredentialHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterPaymentProviderHandler
        implements RegisterPaymentProviderUseCase {

    private final PaymentProviderCommandRepository
            paymentProviderCommandRepository;

    private final PaymentProviderClientIdGenerator
            clientIdGenerator;

    private final PaymentProviderCredentialGenerator
            credentialGenerator;

    private final PaymentProviderCredentialHashService
            credentialHashService;

    private final ProviderAccountPort providerAccountPort;

    @Override
    @Transactional
    public RegisterPaymentProviderResult execute(
            RegisterPaymentProviderCommand command
    ) {
        validateCommand(command);

        String code = command.code().trim();
        String name = command.name().trim();

        validateCodeNotExists(code);

        String clientId = generateClientId();
        String credential = credentialGenerator.generate();

        String credentialHash =
                credentialHashService.hash(credential);

        Instant now = Instant.now();

        PaymentProvider provider =
                PaymentProvider.builder()
                        .code(code)
                        .name(name)
                        .type(command.type())
                        .status(ProviderStatus.ACTIVE)
                        .clientId(clientId)
                        .credentialHash(credentialHash)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        PaymentProvider savedProvider =
                paymentProviderCommandRepository.save(provider);

        createSettlementAccounts(savedProvider.getId());

        return new RegisterPaymentProviderResult(
                savedProvider.getId(),
                savedProvider.getCode(),
                savedProvider.getName(),
                savedProvider.getType(),
                savedProvider.getStatus(),
                savedProvider.getClientId(),
                credential
        );
    }

    private void validateCommand(
            RegisterPaymentProviderCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.code() == null
                || command.code().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_CODE_REQUIRED
            );
        }

        if (command.name() == null
                || command.name().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_NAME_REQUIRED
            );
        }

        if (command.type() == null) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_TYPE_REQUIRED
            );
        }
    }

    private void validateCodeNotExists(
            String code
    ) {
        if (paymentProviderCommandRepository
                .existsByCode(code)) {

            throw new BusinessException(
                    ErrorCode.PROVIDER_CODE_ALREADY_EXISTS
            );
        }
    }

    private String generateClientId() {
        String clientId;

        do {
            clientId = clientIdGenerator.generate();
        } while (
                paymentProviderCommandRepository
                        .existsByClientId(clientId)
        );

        return clientId;
    }

    private void createSettlementAccounts(
            UUID providerId
    ) {
        for (Currency currency : Currency.values()) {
            providerAccountPort.createProviderAccount(
                    providerId,
                    currency
            );
        }
    }
}