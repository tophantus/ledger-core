package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.port.inbound.GetWebhooksUseCase;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWebhooksHandler implements GetWebhooksUseCase {

    private final AccountOwnerPort accountOwnerPort;
    private final WebhookEndpointQueryRepository webhookEndpointQueryRepository;
    private final WebhookSubscriptionQueryRepository webhookSubscriptionQueryRepository;
    private final WebhookResponseMapper webhookResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WebhookResponse> execute(
            UUID userId,
            UUID accountId
    ) {
        accountOwnerPort.verifyOwnership(
                userId,
                accountId
        );

        List<WebhookEndpoint> endpoints =
                webhookEndpointQueryRepository.findAllByAccountId(
                        accountId
                );

        if (endpoints.isEmpty()) {
            return List.of();
        }

        List<UUID> endpointIds = endpoints.stream()
                .map(WebhookEndpoint::getId)
                .toList();

        List<WebhookSubscription> subscriptions =
                webhookSubscriptionQueryRepository
                        .findAllByWebhookEndpointIdIn(endpointIds);

        return webhookResponseMapper.map(
                endpoints,
                subscriptions
        );
    }
}