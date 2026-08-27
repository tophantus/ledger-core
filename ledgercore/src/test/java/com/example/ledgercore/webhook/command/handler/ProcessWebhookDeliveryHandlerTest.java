package com.example.ledgercore.webhook.command.handler;

import com.example.ledgercore.webhook.command.port.outbound.WebhookHttpClientPort;
import com.example.ledgercore.webhook.command.repository.WebhookDeliveryCommandRepository;
import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import com.example.ledgercore.webhook.query.repository.WebhookDeliveryQueryRepository;
import com.example.ledgercore.webhook.query.repository.WebhookEndpointQueryRepository;
import com.example.ledgercore.webhook.service.WebhookRetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessWebhookDeliveryHandlerTest {

    @Mock
    private WebhookDeliveryQueryRepository webhookDeliveryQueryRepository;

    @Mock
    private WebhookDeliveryCommandRepository webhookDeliveryCommandRepository;

    @Mock
    private WebhookEndpointQueryRepository webhookEndpointQueryRepository;

    @Mock
    private WebhookHttpClientPort webhookHttpClientPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebhookRetryPolicy retryPolicy;

    @Mock
    private WebhookDelivery delivery;

    @Mock
    private WebhookEndpoint endpoint;

    @Mock
    private JsonNode jsonNode;

    private ProcessWebhookDeliveryHandler handler;

    private UUID deliveryId;
    private UUID endpointId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        handler = new ProcessWebhookDeliveryHandler(
                webhookDeliveryQueryRepository,
                webhookDeliveryCommandRepository,
                webhookEndpointQueryRepository,
                webhookHttpClientPort,
                objectMapper,
                retryPolicy
        );

        deliveryId = UUID.randomUUID();
        endpointId = UUID.randomUUID();
        eventId = UUID.randomUUID();
    }

    @Test
    void shouldReturn_whenDeliveryCannotBeClaimed() {

        when(webhookDeliveryCommandRepository.claim(
                eq(deliveryId),
                eq(WebhookDeliveryStatus.PROCESSING),
                eq(WebhookDeliveryStatus.PENDING),
                eq(WebhookDeliveryStatus.RETRYING),
                any(Instant.class)
        )).thenReturn(0);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .claim(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq(WebhookDeliveryStatus.PENDING),
                        eq(WebhookDeliveryStatus.RETRYING),
                        any(Instant.class)
                );

        verifyNoMoreInteractions(
                webhookDeliveryQueryRepository,
                webhookEndpointQueryRepository,
                webhookHttpClientPort,
                objectMapper,
                retryPolicy
        );
    }

    @Test
    void shouldReturn_whenDeliveryIsNotFoundAfterClaim() {

        givenClaimSuccessful();

        when(webhookDeliveryQueryRepository.findById(deliveryId))
                .thenReturn(Optional.empty());

        handler.execute(deliveryId);

        verify(webhookDeliveryQueryRepository)
                .findById(deliveryId);

        verifyNoInteractions(
                webhookEndpointQueryRepository,
                webhookHttpClientPort,
                objectMapper,
                retryPolicy
        );

        verify(webhookDeliveryCommandRepository, never())
                .markFailed(
                        any(),
                        any(),
                        any(),
                        anyString()
                );
    }

    @Test
    void shouldMarkFailed_whenEndpointIsNotFound() {

        givenClaimSuccessful();
        givenDelivery();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.empty());

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook endpoint not found")
                );

        verifyNoInteractions(
                webhookHttpClientPort,
                objectMapper,
                retryPolicy
        );
    }

    @Test
    void shouldMarkFailed_whenEndpointIsNotActive() {

        givenClaimSuccessful();
        givenDelivery();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(endpoint.getStatus())
                .thenReturn(WebhookStatus.INACTIVE);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook endpoint is not active")
                );

        verifyNoInteractions(
                webhookHttpClientPort,
                objectMapper,
                retryPolicy
        );
    }

    @Test
    void shouldMarkDelivered_whenWebhookReturnsSuccess() throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        200,
                        "{\"ok\":true}",
                        null
                );

        when(webhookHttpClientPort.send(
                eq("https://example.com/webhook"),
                eq("secret"),
                eq("{\"payload\":\"test\"}")
        )).thenReturn(response);

        handler.execute(deliveryId);

        verify(webhookHttpClientPort)
                .send(
                        eq("https://example.com/webhook"),
                        eq("secret"),
                        eq("{\"payload\":\"test\"}")
                );

        verify(webhookDeliveryCommandRepository)
                .markDelivered(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.DELIVERED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        any(Instant.class)
                );

        verify(webhookDeliveryCommandRepository, never())
                .markFailed(any(), any(), any(), anyString());

        verify(webhookDeliveryCommandRepository, never())
                .markRetry(
                        any(),
                        any(),
                        any(),
                        any(Instant.class),
                        anyString()
                );

        verifyNoInteractions(retryPolicy);
    }

    @Test
    void shouldMarkDelivered_whenWebhookReturnsAny2xxStatus()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        204,
                        "",
                        null
                );

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markDelivered(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.DELIVERED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        any(Instant.class)
                );

        verify(webhookDeliveryCommandRepository, never())
                .markFailed(any(), any(), any(), anyString());

        verify(webhookDeliveryCommandRepository, never())
                .markRetry(
                        any(),
                        any(),
                        any(),
                        any(Instant.class),
                        anyString()
                );
    }

    @Test
    void shouldMarkFailed_whenWebhookReturnsNonRetryableError()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        400,
                        "{\"error\":\"invalid request\"}",
                        null
                );

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook returned HTTP 400: {\"error\":\"invalid request\"}")
                );

        verify(webhookDeliveryCommandRepository, never())
                .markRetry(
                        any(),
                        any(),
                        any(),
                        any(Instant.class),
                        anyString()
                );

        verifyNoInteractions(retryPolicy);
    }

    @Test
    void shouldUseResponseError_whenWebhookResponseContainsError()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        400,
                        "{\"ignored\":true}",
                        "Connection rejected"
                );

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Connection rejected")
                );
    }

    @Test
    void shouldScheduleRetry_whenWebhookErrorIsRetryable()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        when(delivery.getAttemptCount())
                .thenReturn(1);

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        500,
                        "internal server error",
                        null
                );

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        when(retryPolicy.shouldRetry(1))
                .thenReturn(true);

        Duration delay = Duration.ofSeconds(30);

        when(retryPolicy.getDelay(1))
                .thenReturn(delay);

        handler.execute(deliveryId);

        verify(retryPolicy)
                .shouldRetry(1);

        verify(retryPolicy)
                .getDelay(1);

        verify(webhookDeliveryCommandRepository)
                .markRetry(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.RETRYING),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        any(Instant.class),
                        eq("Webhook returned HTTP 500: internal server error")
                );

        verify(webhookDeliveryCommandRepository, never())
                .markFailed(
                        any(),
                        any(),
                        any(),
                        anyString()
                );

        verify(webhookDeliveryCommandRepository, never())
                .markDelivered(
                        any(),
                        any(),
                        any(),
                        any(Instant.class)
                );
    }

    @Test
    void shouldMarkFailed_whenRetryLimitIsReached()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        when(delivery.getAttemptCount())
                .thenReturn(5);

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        500,
                        "server error",
                        null
                );

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        when(retryPolicy.shouldRetry(5))
                .thenReturn(false);

        handler.execute(deliveryId);

        verify(retryPolicy)
                .shouldRetry(5);

        verify(retryPolicy, never())
                .getDelay(anyInt());

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook returned HTTP 500: server error")
                );

        verify(webhookDeliveryCommandRepository, never())
                .markRetry(
                        any(),
                        any(),
                        any(),
                        any(Instant.class),
                        anyString()
                );
    }

    @Test
    void shouldRetry_whenWebhookReturns408()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        when(delivery.getAttemptCount())
                .thenReturn(1);

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(
                new WebhookHttpClientPort.WebhookResponse(
                        408,
                        "",
                        null
                )
        );

        when(retryPolicy.shouldRetry(1))
                .thenReturn(false);

        handler.execute(deliveryId);

        verify(retryPolicy)
                .shouldRetry(1);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook returned HTTP 408")
                );
    }

    @Test
    void shouldRetry_whenWebhookReturns429()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        when(delivery.getAttemptCount())
                .thenReturn(1);

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(
                new WebhookHttpClientPort.WebhookResponse(
                        429,
                        "",
                        null
                )
        );

        when(retryPolicy.shouldRetry(1))
                .thenReturn(false);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook returned HTTP 429")
                );
    }

    @Test
    void shouldRetry_whenWebhookStatusIsZero()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        when(delivery.getAttemptCount())
                .thenReturn(1);

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(
                new WebhookHttpClientPort.WebhookResponse(
                        0,
                        null,
                        "Connection refused"
                )
        );

        when(retryPolicy.shouldRetry(1))
                .thenReturn(false);

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Connection refused")
                );
    }

    @Test
    void shouldBuildWebhookPayloadBeforeSending()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();

        when(delivery.getPayload())
                .thenReturn("{\"amount\":100}");

        when(delivery.getEventId())
                .thenReturn(eventId);

        when(delivery.getEventType())
                .thenReturn(WebhookEventType.TRANSACTION_COMPLETED);

        when(objectMapper.readTree("{\"amount\":100}"))
                .thenReturn(jsonNode);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn(
                        "{\"eventId\":\""
                                + eventId
                                + "\",\"type\":\"TRANSACTION_COMPLETED\"}"
                );

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        200,
                        null,
                        null
                );

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        handler.execute(deliveryId);

        verify(objectMapper)
                .readTree("{\"amount\":100}");

        verify(objectMapper)
                .writeValueAsString(any());

        verify(webhookHttpClientPort)
                .send(
                        eq("https://example.com/webhook"),
                        eq("secret"),
                        eq(
                                "{\"eventId\":\""
                                        + eventId
                                        + "\",\"type\":\"TRANSACTION_COMPLETED\"}"
                        )
                );
    }

    // =========================================================
    // PAYLOAD BUILD FAILURE
    // =========================================================

    @Test
    void shouldMarkFailed_whenPayloadCannotBeBuilt()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(endpoint.getId())
                .thenReturn(endpointId);

        when(endpoint.getStatus())
                .thenReturn(WebhookStatus.ACTIVE);

        when(endpoint.getUrl())
                .thenReturn("https://example.com/webhook");

        when(delivery.getPayload())
                .thenReturn("{invalid-json");

        when(objectMapper.readTree("{invalid-json"))
                .thenThrow(new JacksonException("Invalid JSON") {});

        handler.execute(deliveryId);

        verify(objectMapper)
                .readTree("{invalid-json");

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Failed to build webhook payload")
                );

        verifyNoInteractions(
                webhookHttpClientPort,
                retryPolicy
        );

        verify(objectMapper, never())
                .writeValueAsString(any());
    }

    @Test
    void shouldTruncateLongError_whenResponseErrorIsTooLong()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        String longError = "x".repeat(3000);

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(
                new WebhookHttpClientPort.WebhookResponse(
                        400,
                        null,
                        longError
                )
        );

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        argThat(error ->
                                error.length() == 2000
                        )
                );
    }

    @Test
    void shouldUseUnknownError_whenErrorAndBodyAreEmpty()
            throws Exception {

        givenClaimSuccessful();
        givenDelivery();
        givenActiveEndpoint();
        givenValidPayload();

        when(webhookHttpClientPort.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(
                new WebhookHttpClientPort.WebhookResponse(
                        400,
                        "",
                        ""
                )
        );

        handler.execute(deliveryId);

        verify(webhookDeliveryCommandRepository)
                .markFailed(
                        eq(deliveryId),
                        eq(WebhookDeliveryStatus.FAILED),
                        eq(WebhookDeliveryStatus.PROCESSING),
                        eq("Webhook returned HTTP 400")
                );
    }

    @Test
    void shouldRecognize2xxAsSuccess() {

        assertSuccess(200);
        assertSuccess(201);
        assertSuccess(202);
        assertSuccess(204);
    }

    @Test
    void shouldRecognizeRetryableStatusCodes() {

        assertRetryable(408);
        assertRetryable(429);
        assertRetryable(500);
        assertRetryable(502);
        assertRetryable(503);
        assertRetryable(599);
        assertRetryable(0);
    }

    @Test
    void shouldRecognizeNonRetryableStatusCodes() {

        assertNonRetryable(400);
        assertNonRetryable(401);
        assertNonRetryable(403);
        assertNonRetryable(404);
        assertNonRetryable(409);
        assertNonRetryable(422);
    }

    private void givenClaimSuccessful() {

        when(webhookDeliveryCommandRepository.claim(
                eq(deliveryId),
                eq(WebhookDeliveryStatus.PROCESSING),
                eq(WebhookDeliveryStatus.PENDING),
                eq(WebhookDeliveryStatus.RETRYING),
                any(Instant.class)
        )).thenReturn(1);
    }

    private void givenDelivery() {

        when(webhookDeliveryQueryRepository.findById(deliveryId))
                .thenReturn(Optional.of(delivery));

        when(delivery.getId())
                .thenReturn(deliveryId);

        when(delivery.getWebhookEndpointId())
                .thenReturn(endpointId);

        when(delivery.getAttemptCount())
                .thenReturn(1);
    }

    private void givenActiveEndpoint() {

        when(webhookEndpointQueryRepository.findById(endpointId))
                .thenReturn(Optional.of(endpoint));

        when(endpoint.getId())
                .thenReturn(endpointId);

        when(endpoint.getStatus())
                .thenReturn(WebhookStatus.ACTIVE);

        when(endpoint.getUrl())
                .thenReturn("https://example.com/webhook");

        when(endpoint.getSecret())
                .thenReturn("secret");
    }

    private void givenValidPayload() throws Exception {

        when(delivery.getPayload())
                .thenReturn("{\"amount\":100}");

        when(delivery.getEventId())
                .thenReturn(eventId);

        when(delivery.getEventType())
                .thenReturn(WebhookEventType.TRANSACTION_COMPLETED);

        when(objectMapper.readTree("{\"amount\":100}"))
                .thenReturn(jsonNode);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"payload\":\"test\"}");
    }

    private void assertSuccess(int statusCode) {

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        statusCode,
                        null,
                        null
                );

        org.junit.jupiter.api.Assertions.assertTrue(
                response.isSuccess()
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                response.isRetryable()
        );
    }

    private void assertRetryable(int statusCode) {

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        statusCode,
                        null,
                        null
                );

        org.junit.jupiter.api.Assertions.assertTrue(
                response.isRetryable()
        );
    }

    private void assertNonRetryable(int statusCode) {

        WebhookHttpClientPort.WebhookResponse response =
                new WebhookHttpClientPort.WebhookResponse(
                        statusCode,
                        null,
                        null
                );

        org.junit.jupiter.api.Assertions.assertFalse(
                response.isRetryable()
        );
    }
}