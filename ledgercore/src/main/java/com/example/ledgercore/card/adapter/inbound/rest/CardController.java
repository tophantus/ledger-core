package com.example.ledgercore.card.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.card.adapter.inbound.rest.dto.CreateDebitCardRequest;
import com.example.ledgercore.card.command.dto.CreateDebitCardCommand;
import com.example.ledgercore.card.command.dto.CreateDebitCardResult;
import com.example.ledgercore.card.command.port.inbound.CreateDebitCardUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(
        name = "Card",
        description = "Card management APIs"
)
public class CardController {

    private final CreateDebitCardUseCase createDebitCardUseCase;

    @PostMapping("/debit")
    @Operation(
            summary = "Create debit card",
            description = "Create a debit card linked to the authenticated customer's account"
    )
    public ResponseEntity<ApiResponse<CreateDebitCardResult>> createDebitCard(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateDebitCardRequest request
    ) {
        UUID customerId = principal.getUserId();

        CreateDebitCardResult result =
                createDebitCardUseCase.execute(
                        new CreateDebitCardCommand(
                                customerId,
                                request.accountId(),
                                request.pin()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Debit card created successfully"
                )
        );
    }
}