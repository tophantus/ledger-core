package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.port.inbound.GetWebhookUseCase;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWebhookHandler implements GetWebhookUseCase {

    private final AccountOwnerPort accountOwnerPort;
    private final WebhookEndpointQueryRepository webhookEndpointQueryRepository;
    private final WebhookSubscriptionQueryRepository webhookSubscriptionQueryRepository;
    private final WebhookResponseMapper webhookResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public WebhookResponse execute(
            UUID userId,
            UUID webhookId
    ) {
        WebhookEndpoint endpoint =
                webhookEndpointQueryRepository.findById(webhookId)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.WEBHOOK_NOT_FOUND
                        ));

        accountOwnerPort.verifyOwnership(
                userId,
                endpoint.getAccountId()
        );

        List<com.example.ledgercore.webhook.entity.WebhookSubscription> subscriptions =
                webhookSubscriptionQueryRepository
                        .findAllByWebhookEndpointId(endpoint.getId());

        return webhookResponseMapper.map(
                endpoint,
                subscriptions
        );
    }
}