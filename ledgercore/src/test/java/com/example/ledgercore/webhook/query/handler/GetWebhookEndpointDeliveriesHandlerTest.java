package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.query.dto.GetWebhookEndpointDeliveriesQuery;
import com.example.ledgercore.webhook.query.dto.WebhookDeliveryResponse;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.port.outbound.WebhookAccountOwnerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWebhookEndpointDeliveriesHandlerTest {

    @Mock
    private WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    @Mock
    private WebhookDeliveryQueryRepository
            webhookDeliveryQueryRepository;

    @Mock
    private WebhookAccountOwnerPort webhookAccountOwnerPort;

    @InjectMocks
    private GetWebhookEndpointDeliveriesHandler handler;

    private UUID userId;
    private UUID endpointId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        endpointId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnPagedDeliveries() {
        WebhookEndpoint endpoint =
                endpoint();

        WebhookDelivery delivered =
                delivery(
                        WebhookDeliveryStatus.DELIVERED,
                        WebhookEventType.TRANSACTION_COMPLETED
                );

        WebhookDelivery failed =
                delivery(
                        WebhookDeliveryStatus.FAILED,
                        WebhookEventType.TRANSACTION_FAILED
                );

        Page<WebhookDelivery> page =
                new PageImpl<>(
                        List.of(delivered, failed),
                        PageRequest.of(0, 20),
                        2
                );

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(webhookDeliveryQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        null,
                        null,
                        0,
                        20
                );

        PageResponse<WebhookDeliveryResponse> response =
                handler.execute(query);

        assertEquals(2, response.content().size());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());

        assertEquals(
                delivered.getId(),
                response.content().get(0).id()
        );
        assertEquals(
                WebhookDeliveryStatus.DELIVERED,
                response.content().get(0).status()
        );

        assertEquals(
                failed.getId(),
                response.content().get(1).id()
        );
        assertEquals(
                WebhookDeliveryStatus.FAILED,
                response.content().get(1).status()
        );

        verify(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookDeliveryQueryRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldMapProcessingStatusToPending() {
        WebhookEndpoint endpoint =
                endpoint();

        WebhookDelivery processing =
                delivery(
                        WebhookDeliveryStatus.PROCESSING,
                        WebhookEventType.TRANSACTION_COMPLETED
                );

        Page<WebhookDelivery> page =
                new PageImpl<>(List.of(processing));

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(webhookDeliveryQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        WebhookDeliveryStatus.PENDING,
                        null,
                        0,
                        20
                );

        PageResponse<WebhookDeliveryResponse> response =
                handler.execute(query);

        assertEquals(1, response.content().size());
        assertEquals(
                WebhookDeliveryStatus.PENDING,
                response.content().getFirst().status()
        );
    }

    @Test
    void shouldUseRequestedPaginationAndSort() {
        WebhookEndpoint endpoint =
                endpoint();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(webhookDeliveryQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(
                        List.of(),
                        org.springframework.data.domain.PageRequest.of(
                                2,
                                10
                        ),
                        25
                )
        );

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        null,
                        null,
                        2,
                        10
                );

        handler.execute(query);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(webhookDeliveryQueryRepository)
                .findAll(
                        any(Specification.class),
                        pageableCaptor.capture()
                );

        Pageable pageable =
                pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                "createdAt",
                pageable.getSort().getOrderFor("createdAt").getProperty()
        );
        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getSort().getOrderFor("createdAt").getDirection()
        );

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void shouldVerifyEndpointAccountOwnership() {
        WebhookEndpoint endpoint =
                endpoint();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(webhookDeliveryQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        null,
                        null,
                        0,
                        20
                );

        handler.execute(query);

        verify(webhookAccountOwnerPort)
                .verifyOwnership(
                        userId,
                        accountId
                );
    }

    @Test
    void shouldThrowWhenEndpointNotFound() {
        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.empty());

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        null,
                        null,
                        0,
                        20
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(query)
        );

        verify(webhookAccountOwnerPort, never())
                .verifyOwnership(any(), any());

        verify(webhookDeliveryQueryRepository, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldNotQueryDeliveriesWhenOwnershipVerificationFails() {
        WebhookEndpoint endpoint =
                endpoint();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        doThrow(BusinessException.class)
                .when(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        null,
                        null,
                        0,
                        20
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(query)
        );

        verify(webhookDeliveryQueryRepository, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldThrowWhenQueryIsNull() {
        assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookDeliveryQueryRepository,
                webhookAccountOwnerPort
        );
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        null,
                        endpointId,
                        null,
                        null,
                        0,
                        20
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(query)
        );

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookDeliveryQueryRepository,
                webhookAccountOwnerPort
        );
    }

    @Test
    void shouldThrowWhenEndpointIdIsNull() {
        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(query)
        );

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookDeliveryQueryRepository,
                webhookAccountOwnerPort
        );
    }

    @Test
    void shouldThrowWhenProcessingStatusIsRequested() {
        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        WebhookDeliveryStatus.PROCESSING,
                        null,
                        0,
                        20
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(query)
        );

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookDeliveryQueryRepository,
                webhookAccountOwnerPort
        );
    }

    @Test
    void shouldReturnEmptyPageWhenNoDeliveriesFound() {
        WebhookEndpoint endpoint =
                endpoint();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(webhookDeliveryQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(
                        List.of(),
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        ),
                        0
                )
        );

        GetWebhookEndpointDeliveriesQuery query =
                new GetWebhookEndpointDeliveriesQuery(
                        userId,
                        endpointId,
                        null,
                        null,
                        0,
                        20
                );

        PageResponse<WebhookDeliveryResponse> response =
                handler.execute(query);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
    }

    private WebhookEndpoint endpoint() {
        return WebhookEndpoint.builder()
                .id(endpointId)
                .accountId(accountId)
                .url("https://example.com/webhook")
                .secret("secret")
                .build();
    }

    private WebhookDelivery delivery(
            WebhookDeliveryStatus status,
            WebhookEventType eventType
    ) {
        return WebhookDelivery.builder()
                .id(UUID.randomUUID())
                .webhookEndpointId(endpointId)
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .payload("{}")
                .status(status)
                .attemptCount(1)
                .nextAttemptAt(
                        status == WebhookDeliveryStatus.RETRYING
                                ? Instant.now()
                                : null
                )
                .createdAt(Instant.now())
                .build();
    }
}
