package com.example.ledgercore.card.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.card.adapter.inbound.rest.dto.CreateCreditCardRequest;
import com.example.ledgercore.card.adapter.inbound.rest.dto.CreateDebitCardRequest;
import com.example.ledgercore.card.command.dto.CreateCreditCardCommand;
import com.example.ledgercore.card.command.dto.CreateCreditCardResult;
import com.example.ledgercore.card.command.dto.CreateDebitCardCommand;
import com.example.ledgercore.card.command.dto.CreateDebitCardResult;
import com.example.ledgercore.card.command.port.inbound.CreateCreditCardUseCase;
import com.example.ledgercore.card.command.port.inbound.CreateDebitCardUseCase;
import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetUserCardsQuery;
import com.example.ledgercore.card.query.port.inbound.GetUserCardsUseCase;
import com.example.ledgercore.common.dto.PageResponse;
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
    private final CreateCreditCardUseCase createCreditCardUseCase;

    private final GetUserCardsUseCase getUserCardsUseCase;

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

    @PostMapping("/credit")
    @Operation(
            summary = "Create credit card",
            description = "Create a credit card linked to the authenticated customer's credit facility"
    )
    public ResponseEntity<ApiResponse<CreateCreditCardResult>> createCreditCard(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateCreditCardRequest request
    ) {
        UUID customerId = principal.getUserId();

        CreateCreditCardResult result =
                createCreditCardUseCase.execute(
                        new CreateCreditCardCommand(
                                customerId,
                                request.creditFacilityId(),
                                request.pin()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Credit card created successfully"
                )
        );
    }

    @GetMapping
    @Operation(
            summary = "Get user cards",
            description = "Get paginated cards of the authenticated customer"
    )
    public ResponseEntity<ApiResponse<PageResponse<CardInfo>>> getUserCards(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<CardInfo> response =
                getUserCardsUseCase.execute(
                        new GetUserCardsQuery(
                                principal.getUserId(),
                                page,
                                size
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Cards retrieved successfully"
                )
        );
    }
}