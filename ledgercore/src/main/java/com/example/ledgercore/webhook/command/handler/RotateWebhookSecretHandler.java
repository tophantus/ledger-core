package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretCommand;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretResult;
import com.example.ledgercore.webhook.command.port.inbound.RotateWebhookSecretUseCase;
import com.example.ledgercore.webhook.command.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.service.WebhookSecretGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RotateWebhookSecretHandler
        implements RotateWebhookSecretUseCase {

    private final AccountOwnerPort accountOwnerPort;
    private final WebhookEndpointCommandRepository webhookEndpointCommandRepository;
    private final WebhookSecretGenerator webhookSecretGenerator;

    @Override
    @Transactional
    public RotateWebhookSecretResult execute(
            RotateWebhookSecretCommand command
    ) {
        WebhookEndpoint endpoint = webhookEndpointCommandRepository
                .findById(command.webhookId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.WEBHOOK_NOT_FOUND
                ));

        accountOwnerPort.verifyOwnership(
                command.userId(),
                endpoint.getAccountId()
        );

        String secret = webhookSecretGenerator.generate();

        endpoint.rotateSecret(secret);

        return new RotateWebhookSecretResult(
                endpoint.getId(),
                secret,
                endpoint.getUpdatedAt()
        );
    }
}