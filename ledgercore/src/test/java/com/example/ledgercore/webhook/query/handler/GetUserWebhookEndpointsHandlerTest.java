package com.example.ledgercore.webhook.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.mapper.WebhookResponseMapper;
import com.example.ledgercore.webhook.query.port.outbound.WebhookUserAccountPort;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookSubscriptionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserWebhookEndpointsHandlerTest {

    @Mock
    private WebhookUserAccountPort webhookUserAccountPort;

    @Mock
    private WebhookEndpointQueryRepository
            webhookEndpointQueryRepository;

    @Mock
    private WebhookSubscriptionQueryRepository
            webhookSubscriptionQueryRepository;

    @Mock
    private WebhookResponseMapper webhookResponseMapper;

    @InjectMocks
    private GetUserWebhookEndpointsHandler handler;

    private UUID userId;
    private UUID accountId1;
    private UUID accountId2;
    private UUID webhookId1;
    private UUID webhookId2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId1 = UUID.randomUUID();
        accountId2 = UUID.randomUUID();
        webhookId1 = UUID.randomUUID();
        webhookId2 = UUID.randomUUID();
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoAccounts() {
        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of());

        PageResponse<WebhookResponse> response =
                handler.execute(userId, 0, 20);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());

        verify(webhookUserAccountPort).getAccountIds(userId);
        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoWebhooks() {
        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of(accountId1));

        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        ),
                        0
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdInAndStatus(
                eq(List.of(accountId1)),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> response =
                handler.execute(userId, 0, 20);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());

        verify(webhookUserAccountPort).getAccountIds(userId);
        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdInAndStatus(
                        eq(List.of(accountId1)),
                        eq(WebhookStatus.ACTIVE),
                        any(Pageable.class)
                );

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldReturnUserWebhooksSuccessfully() {
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

        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of(accountId1, accountId2));

        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(endpoint1, endpoint2),
                        org.springframework.data.domain.PageRequest.of(
                                1,
                                20
                        ),
                        42
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdInAndStatus(
                eq(List.of(accountId1, accountId2)),
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

        PageResponse<WebhookResponse> response =
                handler.execute(userId, 1, 20);

        assertNotNull(response);
        assertEquals(
                List.of(response1, response2),
                response.content()
        );
        assertEquals(1, response.page());
        assertEquals(20, response.size());
        assertEquals(42, response.totalElements());
        assertEquals(3, response.totalPages());

        verify(webhookUserAccountPort).getAccountIds(userId);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdInAndStatus(
                        eq(List.of(accountId1, accountId2)),
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
    void shouldQueryWebhooksUsingCorrectPaginationAndSort() {
        WebhookEndpoint endpoint =
                mock(WebhookEndpoint.class);

        when(endpoint.getId()).thenReturn(webhookId1);

        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of(accountId1));

        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(endpoint),
                        org.springframework.data.domain.PageRequest.of(
                                2,
                                10
                        ),
                        25
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdInAndStatus(
                eq(List.of(accountId1)),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        when(webhookSubscriptionQueryRepository
                .findAllByWebhookEndpointIdIn(
                        eq(List.of(webhookId1))
                ))
                .thenReturn(List.of());

        WebhookResponse webhookResponse =
                mock(WebhookResponse.class);

        when(webhookResponseMapper.map(
                eq(List.of(endpoint)),
                eq(List.of())
        )).thenReturn(List.of(webhookResponse));

        handler.execute(userId, 2, 10);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdInAndStatus(
                        eq(List.of(accountId1)),
                        eq(WebhookStatus.ACTIVE),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                "createdAt: DESC,id: DESC",
                pageable.getSort().toString()
        );
    }

    @Test
    void shouldNormalizeNegativePageToZero() {
        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of());

        PageResponse<WebhookResponse> response =
                handler.execute(userId, -5, 20);

        assertEquals(0, response.page());
        assertEquals(20, response.size());

        verify(webhookUserAccountPort).getAccountIds(userId);
        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldClampSizeToOneWhenSizeIsLessThanOne() {
        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of());

        PageResponse<WebhookResponse> response =
                handler.execute(userId, 0, 0);

        assertEquals(0, response.page());
        assertEquals(1, response.size());

        verify(webhookUserAccountPort).getAccountIds(userId);
        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldClampSizeToOneHundredWhenSizeExceedsMaximum() {
        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of());

        PageResponse<WebhookResponse> response =
                handler.execute(userId, 0, 500);

        assertEquals(0, response.page());
        assertEquals(100, response.size());

        verify(webhookUserAccountPort).getAccountIds(userId);
        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }

    @Test
    void shouldNotQuerySubscriptionsWhenEndpointPageIsEmpty() {
        when(webhookUserAccountPort.getAccountIds(userId))
                .thenReturn(List.of(accountId1, accountId2));

        Page<WebhookEndpoint> endpointPage =
                new PageImpl<>(
                        List.of(),
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        ),
                        5
                );

        when(webhookEndpointQueryRepository.findAllByAccountIdInAndStatus(
                eq(List.of(accountId1, accountId2)),
                eq(WebhookStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(endpointPage);

        PageResponse<WebhookResponse> response =
                handler.execute(userId, 0, 20);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(5, response.totalElements());
        assertEquals(1, response.totalPages());

        verify(webhookEndpointQueryRepository)
                .findAllByAccountIdInAndStatus(
                        eq(List.of(accountId1, accountId2)),
                        eq(WebhookStatus.ACTIVE),
                        any(Pageable.class)
                );

        verifyNoInteractions(
                webhookSubscriptionQueryRepository,
                webhookResponseMapper
        );
    }
}
