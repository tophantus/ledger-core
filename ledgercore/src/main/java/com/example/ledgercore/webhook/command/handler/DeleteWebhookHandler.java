package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.DeleteWebhookCommand;
import com.example.ledgercore.webhook.command.port.inbound.DeleteWebhookUseCase;
import com.example.ledgercore.webhook.port.outbound.WebhookAccountOwnerPort;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteWebhookHandler implements DeleteWebhookUseCase {

    private final WebhookAccountOwnerPort webhookAccountOwnerPort;
    private final WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @Override
    @Transactional
    public void execute(DeleteWebhookCommand command) {
        WebhookEndpoint endpoint = webhookEndpointCommandRepository
                .findById(command.webhookId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.WEBHOOK_ENDPOINT_NOT_FOUND
                ));

        webhookAccountOwnerPort.verifyOwnership(
                command.userId(),
                endpoint.getAccountId()
        );

        endpoint.inactivate();
    }
}