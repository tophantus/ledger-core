package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.port.inbound.GetUserWebhookEndpointsUseCase;
import com.example.ledgercore.webhook.query.port.outbound.WebhookUserAccountPort;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserWebhookEndpointsHandler
        implements GetUserWebhookEndpointsUseCase {

    private final WebhookUserAccountPort webhookUserAccountPort;

    private final WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    private final WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    private final WebhookResponseMapper webhookResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WebhookResponse> execute(
            UUID userId,
            int page,
            int size
    ) {
        List<UUID> accountIds =
                webhookUserAccountPort.getAccountIds(userId);

        if (accountIds.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    Math.max(page, 0),
                    Math.clamp(size, 1, 100),
                    0,
                    0
            );
        }

        Pageable pageable =
                PageRequest.of(
                        Math.max(page, 0),
                        Math.clamp(size, 1, 100),
                        Sort.by(
                                Sort.Order.desc("createdAt"),
                                Sort.Order.desc("id")
                        )
                );

        Page<WebhookEndpoint> endpointPage =
                webhookEndpointQueryRepository
                        .findAllByAccountIdIn(
                                accountIds,
                                pageable
                        );

        List<WebhookEndpoint> endpoints =
                endpointPage.getContent();

        if (endpoints.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    endpointPage.getNumber(),
                    endpointPage.getSize(),
                    endpointPage.getTotalElements(),
                    endpointPage.getTotalPages()
            );
        }

        List<UUID> endpointIds =
                endpoints.stream()
                        .map(WebhookEndpoint::getId)
                        .toList();

        List<WebhookSubscription> subscriptions =
                webhookSubscriptionQueryRepository
                        .findAllByWebhookEndpointIdIn(endpointIds);

        List<WebhookResponse> content =
                webhookResponseMapper.map(
                        endpoints,
                        subscriptions
                );

        return new PageResponse<>(
                content,
                endpointPage.getNumber(),
                endpointPage.getSize(),
                endpointPage.getTotalElements(),
                endpointPage.getTotalPages()
        );
    }
}