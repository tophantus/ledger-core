package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.query.dto.GetWebhookEndpointDeliveriesQuery;
import com.example.ledgercore.webhook.query.dto.WebhookDeliveryResponse;
import com.example.ledgercore.webhook.query.port.inbound.GetWebhookEndpointDeliveriesUseCase;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.port.outbound.AccountOwnerPort;
import com.example.ledgercore.webhook.query.specification.WebhookDeliverySpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWebhookEndpointDeliveriesHandler
        implements GetWebhookEndpointDeliveriesUseCase {

    private final WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    private final WebhookDeliveryQueryRepository
            webhookDeliveryQueryRepository;

    private final AccountOwnerPort
            accountOwnerPort;

    @Override
    public PageResponse<WebhookDeliveryResponse> execute(
            GetWebhookEndpointDeliveriesQuery query
    ) {
        validateQuery(query);

        WebhookEndpoint endpoint =
                webhookEndpointQueryRepository
                        .findById(query.webhookEndpointId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WEBHOOK_ENDPOINT_NOT_FOUND
                                )
                        );

        accountOwnerPort.verifyOwnership(
                query.userId(),
                endpoint.getAccountId()
        );

        Specification<WebhookDelivery> specification =
                Specification
                        .where(
                                WebhookDeliverySpecifications
                                        .hasEndpointId(
                                                endpoint.getId()
                                        )
                        )
                        .and(
                                WebhookDeliverySpecifications
                                        .hasStatus(
                                                query.status()
                                        )
                        )
                        .and(
                                WebhookDeliverySpecifications
                                        .hasEventType(
                                                query.eventType()
                                        )
                        );

        Pageable pageable =
                PageRequest.of(
                        query.page(),
                        query.size(),
                        Sort.by(
                                Sort.Order.desc("createdAt"),
                                Sort.Order.desc("id")
                        )
                );

        Page<WebhookDelivery> page =
                webhookDeliveryQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private WebhookDeliveryResponse toResponse(
            WebhookDelivery delivery
    ) {
        WebhookDeliveryStatus status =
                delivery.getStatus();

        if (status == WebhookDeliveryStatus.PROCESSING) {
            status = WebhookDeliveryStatus.PENDING;
        }

        return new WebhookDeliveryResponse(
                delivery.getId(),
                delivery.getWebhookEndpointId(),
                delivery.getEventId(),
                delivery.getEventType(),
                status,
                delivery.getAttemptCount(),
                delivery.getNextAttemptAt(),
                delivery.getDeliveredAt(),
                delivery.getLastError(),
                delivery.getCreatedAt()
        );
    }

    private void validateQuery(
            GetWebhookEndpointDeliveriesQuery query
    ) {
        if (query == null
                || query.userId() == null
                || query.webhookEndpointId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.status()
                == WebhookDeliveryStatus.PROCESSING) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}