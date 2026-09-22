package com.example.ledgercore.provider.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderResult;
import com.example.ledgercore.provider.command.port.inbound.AuthenticatePaymentProviderUseCase;
import com.example.ledgercore.provider.command.repository.PaymentProviderCommandRepository;
import com.example.ledgercore.provider.command.service.PaymentProviderCredentialHashService;
import com.example.ledgercore.provider.entity.PaymentProvider;
import com.example.ledgercore.provider.enums.ProviderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticatePaymentProviderHandler
        implements AuthenticatePaymentProviderUseCase {

    private final PaymentProviderCommandRepository
            paymentProviderCommandRepository;

    private final PaymentProviderCredentialHashService
            credentialHashService;

    @Override
    @Transactional(readOnly = true)
    public AuthenticatePaymentProviderResult execute(
            AuthenticatePaymentProviderCommand command
    ) {
        validateCommand(command);

        PaymentProvider provider =
                paymentProviderCommandRepository
                        .findByClientId(command.clientId().trim())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.PROVIDER_AUTHENTICATION_FAILED
                                )
                        );

        if (provider.getStatus() != ProviderStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_NOT_ACTIVE
            );
        }

        if (!credentialHashService.matches(
                command.credential(),
                provider.getCredentialHash()
        )) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_AUTHENTICATION_FAILED
            );
        }

        return new AuthenticatePaymentProviderResult(
                provider.getId(),
                provider.getCode(),
                provider.getName(),
                provider.getType(),
                provider.getStatus()
        );
    }

    private void validateCommand(
            AuthenticatePaymentProviderCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.clientId() == null
                || command.clientId().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_CLIENT_ID_REQUIRED
            );
        }

        if (command.credential() == null
                || command.credential().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_CREDENTIAL_REQUIRED
            );
        }
    }
}