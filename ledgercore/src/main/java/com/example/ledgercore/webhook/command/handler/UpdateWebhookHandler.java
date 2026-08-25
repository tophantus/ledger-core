package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookCommand;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookResult;
import com.example.ledgercore.webhook.command.port.inbound.UpdateWebhookUseCase;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.command.repository.WebhookEndpointCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class UpdateWebhookHandler implements UpdateWebhookUseCase {

    private final AccountOwnerPort accountOwnerPort;
    private final WebhookEndpointCommandRepository webhookEndpointCommandRepository;

    @Override
    @Transactional
    public UpdateWebhookResult execute(UpdateWebhookCommand command) {
        WebhookEndpoint endpoint =
                webhookEndpointCommandRepository.findById(command.webhookId())
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.WEBHOOK_NOT_FOUND
                        ));

        accountOwnerPort.verifyOwnership(
                command.userId(),
                endpoint.getAccountId()
        );

        validateUrl(command.url());

        endpoint.updateUrl(command.url());

        return new UpdateWebhookResult(
                endpoint.getId(),
                endpoint.getAccountId(),
                endpoint.getUrl(),
                endpoint.getStatus(),
                endpoint.getUpdatedAt()
        );
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_WEBHOOK_URL
            );
        }

        try {
            URI uri = URI.create(url);

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_WEBHOOK_URL
                );
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_WEBHOOK_URL
            );
        }
    }
}