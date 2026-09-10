package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.webhook.command.dto.DeleteWebhookCommand;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookCommand;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookResult;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretCommand;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretResult;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookCommand;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookResult;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookSubscriptionsCommand;
import com.example.ledgercore.webhook.command.port.inbound.DeleteWebhookUseCase;
import com.example.ledgercore.webhook.command.port.inbound.RegisterWebhookUseCase;
import com.example.ledgercore.webhook.command.port.inbound.RotateWebhookSecretUseCase;
import com.example.ledgercore.webhook.command.port.inbound.UpdateWebhookSubscriptionsUseCase;
import com.example.ledgercore.webhook.command.port.inbound.UpdateWebhookUseCase;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.query.dto.GetWebhookEndpointDeliveriesQuery;
import com.example.ledgercore.webhook.query.dto.WebhookDeliveryResponse;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import com.example.ledgercore.webhook.query.port.inbound.GetUserWebhookEndpointsUseCase;
import com.example.ledgercore.webhook.query.port.inbound.GetWebhookEndpointDeliveriesUseCase;
import com.example.ledgercore.webhook.query.port.inbound.GetWebhookUseCase;
import com.example.ledgercore.webhook.query.port.inbound.GetAccountWebhookEndpointsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "Webhooks",
        description = "Webhook management APIs"
)
public class WebhookController {

    private final RegisterWebhookUseCase registerWebhookUseCase;
    private final UpdateWebhookUseCase updateWebhookUseCase;
    private final UpdateWebhookSubscriptionsUseCase updateWebhookSubscriptionsUseCase;
    private final DeleteWebhookUseCase deleteWebhookUseCase;
    private final RotateWebhookSecretUseCase rotateWebhookSecretUseCase;

    private final GetWebhookUseCase getWebhookUseCase;
    private final GetUserWebhookEndpointsUseCase getUserWebhookEndpointsUseCase;
    private final GetAccountWebhookEndpointsUseCase getAccountWebhookEndpointsUseCase;
    private final GetWebhookEndpointDeliveriesUseCase
            getWebhookEndpointDeliveriesUseCase;

    @PostMapping("/api/v1/accounts/{accountId}/webhooks")
    @Operation(
            summary = "Register webhook",
            description = "Register a new webhook endpoint for an account"
    )
    public ResponseEntity<ApiResponse<RegisterWebhookResponse>> register(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID accountId,
            @Valid @RequestBody RegisterWebhookRequest request
    ) {
        RegisterWebhookResult result =
                registerWebhookUseCase.execute(
                        new RegisterWebhookCommand(
                                principal.getUserId(),
                                accountId,
                                request.url(),
                                request.eventTypes()
                        )
                );

        RegisterWebhookResponse response =
                new RegisterWebhookResponse(
                        result.webhookId(),
                        result.accountId(),
                        result.url(),
                        result.secret(),
                        result.status(),
                        result.eventTypes(),
                        result.createdAt()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Webhook registered successfully"
                )
        );
    }

    @PatchMapping("/api/v1/webhooks/{webhookId}")
    @Operation(
            summary = "Update webhook",
            description = "Update webhook URL"
    )
    public ResponseEntity<ApiResponse<UpdateWebhookResponse>> updateWebhook(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID webhookId,
            @Valid @RequestBody UpdateWebhookRequest request
    ) {
        UpdateWebhookResult result =
                updateWebhookUseCase.execute(
                        new UpdateWebhookCommand(
                                principal.getUserId(),
                                webhookId,
                                request.url()
                        )
                );

        UpdateWebhookResponse response =
                new UpdateWebhookResponse(
                        result.id(),
                        result.accountId(),
                        result.url(),
                        result.status(),
                        result.updatedAt()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Webhook updated successfully"
                )
        );
    }

    @PutMapping("/api/v1/webhooks/{webhookId}/subscriptions")
    @Operation(
            summary = "Update webhook subscriptions",
            description = "Replace the event subscriptions of a webhook"
    )
    public ResponseEntity<ApiResponse<Void>> updateSubscriptions(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID webhookId,
            @Valid @RequestBody UpdateWebhookSubscriptionsRequest request
    ) {
        updateWebhookSubscriptionsUseCase.execute(
                new UpdateWebhookSubscriptionsCommand(
                        principal.getUserId(),
                        webhookId,
                        request.eventTypes()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Webhook subscriptions updated successfully"
                )
        );
    }

    @PostMapping("/api/v1/webhooks/{webhookId}/secret/rotate")
    @Operation(
            summary = "Rotate webhook secret",
            description = "Generate a new secret for a webhook"
    )
    public ResponseEntity<ApiResponse<RotateWebhookSecretResponse>> rotateSecret(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID webhookId
    ) {
        RotateWebhookSecretResult result =
                rotateWebhookSecretUseCase.execute(
                        new RotateWebhookSecretCommand(
                                principal.getUserId(),
                                webhookId
                        )
                );

        RotateWebhookSecretResponse response =
                new RotateWebhookSecretResponse(
                        result.webhookId(),
                        result.secret(),
                        result.rotatedAt()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Webhook secret rotated successfully"
                )
        );
    }

    @DeleteMapping("/api/v1/webhooks/{webhookId}")
    @Operation(
            summary = "Delete webhook",
            description = "Disable a webhook endpoint"
    )
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID webhookId
    ) {
        deleteWebhookUseCase.execute(
                new DeleteWebhookCommand(
                        principal.getUserId(),
                        webhookId
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Webhook deleted successfully"
                )
        );
    }

    @GetMapping("/api/v1/webhooks")
    @Operation(
            summary = "Get webhooks",
            description = "Get paginated webhooks for the authenticated user or a specific account"
    )
    public ResponseEntity<ApiResponse<PageResponse<WebhookResponse>>> getWebhooks(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<WebhookResponse> response;

        if (accountId != null) {
            response =
                    getAccountWebhookEndpointsUseCase.execute(
                            principal.getUserId(),
                            accountId,
                            page,
                            size
                    );
        } else {
            response =
                    getUserWebhookEndpointsUseCase.execute(
                            principal.getUserId(),
                            page,
                            size
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Webhooks retrieved successfully"
                )
        );
    }

    @GetMapping("/api/v1/webhooks/{webhookId}")
    @Operation(
            summary = "Get webhook",
            description = "Get a webhook by webhook ID"
    )
    public ResponseEntity<ApiResponse<WebhookResponse>> getWebhook(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID webhookId
    ) {
        WebhookResponse response =
                getWebhookUseCase.execute(
                        principal.getUserId(),
                        webhookId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Webhook retrieved successfully"
                )
        );
    }

    @GetMapping("/api/v1/webhooks/{webhookId}/deliveries")
    @Operation(
            summary = "Get webhook deliveries",
            description = "Get paginated deliveries of a webhook endpoint"
    )
    public ResponseEntity<ApiResponse<PageResponse<WebhookDeliveryResponse>>> getDeliveries(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID webhookId,
            @RequestParam(required = false) WebhookDeliveryStatus status,
            @RequestParam(required = false) WebhookEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<WebhookDeliveryResponse> response =
                getWebhookEndpointDeliveriesUseCase.execute(
                        new GetWebhookEndpointDeliveriesQuery(
                                principal.getUserId(),
                                webhookId,
                                status,
                                eventType,
                                page,
                                size
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Webhook deliveries retrieved successfully"
                )
        );
    }
}