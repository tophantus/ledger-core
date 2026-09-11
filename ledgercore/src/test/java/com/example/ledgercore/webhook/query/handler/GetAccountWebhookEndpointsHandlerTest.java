package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import com.example.ledgercore.webhook.port.outbound.WebhookAccountOwnerPort;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountWebhookEndpointsHandlerTest {

    @Mock
    private WebhookAccountOwnerPort webhookAccountOwnerPort;

    @Mock
    private WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    @Mock
    private WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    @Mock
    private WebhookResponseMapper webhookResponseMapper;

    @InjectMocks
    private GetAccountWebhookEndpointsHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID webhookId1;
    private UUID webhookId2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        webhookId1 = UUID.randomUUID();
        webhookId2 = UUID.randomUUID();
    }

    @Test
    void shouldReturnAccountWebhooksSuccessfully() {
        WebhookEndpoint endpoint1 = mock(WebhookEndpoint.class);
        WebhookEndpoint endpoint2 = mock(WebhookEndpoint.class);

        WebhookSubscription subscription1 =
                mock(WebhookSubscription.class);
        WebhookSubscription subscription2 =
                mock(WebhookSubscription.class);

        WebhookResponse response1 =
                mock(WebhookResponse.class);
        WebhookResponse response2 =
                mock(WebhookResponse.class);

        when(endpoint1.getId()).thenReturn(webhookId1);
        when(endpoint2.getId()).thenReturn(webhookId2);

        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(endpoint1, endpoint2),
                        PageRequest.of(0, 20),
                        2
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointIdIn(
                        eq(List.of(webhookId1, webhookId2))
                ))
                .thenReturn(List.of(subscription1, subscription2));

        when(webhookResponseMapper.map(
                eq(List.of(endpoint1, endpoint2)),
                eq(List.of(subscription1, subscription2))
        )).thenReturn(List.of(response1, response2));

        PageResponse<WebhookResponse> result =
                handler.execute(
                        userId,
                        accountId,
                        0,
                        20
                );

        assertNotNull(result);
        assertEquals(
                List.of(response1, response2),
                result.content()
        );
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());

        verify(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        any(Pageable.class)
                );

        verify(webhookSubscriptionQueryRepository)
                .findAllByWebhookEndpointIdIn(
                        eq(List.of(webhookId1, webhookId2))
                );

        verify(webhookResponseMapper)
                .map(
                        eq(List.of(endpoint1, endpoint2)),
                        eq(List.of(subscription1, subscription2))
                );
    }

    @Test
    void shouldReturnEmptyPageWhenAccountHasNoWebhooks() {
        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 20),
                        0
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> result =
                handler.execute(
                        userId,
                        accountId,
                        0,
                        20
                );

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());

        verify(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        any(Pageable.class)
                );

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldVerifyOwnershipBeforeQueryingWebhooks() {
        doThrow(new RuntimeException("Access denied"))
                .when(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        userId,
                        accountId,
                        0,
                        20
                )
        );

        verify(webhookAccountOwnerPort)
                .verifyOwnership(userId, accountId);

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldUseCorrectPaginationAndSort() {
        WebhookEndpoint endpoint =
                mock(WebhookEndpoint.class);

        when(endpoint.getId()).thenReturn(webhookId1);

        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(endpoint),
                        PageRequest.of(2, 10),
                        21
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointIdIn(
                        eq(List.of(webhookId1))
                ))
                .thenReturn(List.of());

        WebhookResponse response =
                mock(WebhookResponse.class);

        when(webhookResponseMapper.map(
                eq(List.of(endpoint)),
                eq(List.of())
        )).thenReturn(List.of(response));

        handler.execute(
                userId,
                accountId,
                2,
                10
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                List.of(
                        new Sort.Order(
                                Sort.Direction.DESC,
                                "createdAt"
                        ),
                        new Sort.Order(
                                Sort.Direction.DESC,
                                "id"
                        )
                ),
                pageable.getSort().toList()
        );
    }

    @Test
    void shouldNormalizeNegativePageToZero() {
        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 20),
                        0
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> result =
                handler.execute(
                        userId,
                        accountId,
                        -5,
                        20
                );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        pageableCaptor.capture()
                );

        assertEquals(
                0,
                pageableCaptor.getValue().getPageNumber()
        );
    }

    @Test
    void shouldClampSizeToOneWhenSizeIsLessThanOne() {
        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 1),
                        0
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> result =
                handler.execute(
                        userId,
                        accountId,
                        0,
                        0
                );

        assertEquals(1, result.size());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        pageableCaptor.capture()
                );

        assertEquals(
                1,
                pageableCaptor.getValue().getPageSize()
        );
    }

    @Test
    void shouldClampSizeToOneHundredWhenSizeExceedsMaximum() {
        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 100),
                        0
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> result =
                handler.execute(
                        userId,
                        accountId,
                        0,
                        500
                );

        assertEquals(100, result.size());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        pageableCaptor.capture()
                );

        assertEquals(
                100,
                pageableCaptor.getValue().getPageSize()
        );
    }

    @Test
    void shouldNotQuerySubscriptionsWhenEndpointPageIsEmpty() {
        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(1, 20),
                        25
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdAndStatus(
                eq(accountId),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> result =
                handler.execute(
                        userId,
                        accountId,
                        1,
                        20
                );

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(1, result.page());
        assertEquals(20, result.size());
        assertEquals(25, result.totalElements());
        assertEquals(2, result.totalPages());

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdAndStatus(
                        eq(accountId),
                        eq(WebhookStatus.ACTIVE),
                        any(Pageable.class)
                );

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }
}
