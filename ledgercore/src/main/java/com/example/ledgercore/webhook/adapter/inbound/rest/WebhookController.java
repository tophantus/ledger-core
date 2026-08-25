package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookCommand;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookResult;
import com.example.ledgercore.webhook.command.port.inbound.RegisterWebhookUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final RegisterWebhookUseCase registerWebhookUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterWebhookResponse register(
            @PathVariable UUID accountId,
            @Valid @RequestBody RegisterWebhookRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
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

        return new RegisterWebhookResponse(
                result.webhookId(),
                result.accountId(),
                result.url(),
                result.secret(),
                result.status(),
                result.eventTypes(),
                result.createdAt()
        );
    }
}