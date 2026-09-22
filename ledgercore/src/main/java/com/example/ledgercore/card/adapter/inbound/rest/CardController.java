package com.example.ledgercore.card.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.card.adapter.inbound.rest.dto.*;
import com.example.ledgercore.card.command.dto.*;
import com.example.ledgercore.card.command.port.inbound.AuthorizeCardPaymentByTokenUseCase;
import com.example.ledgercore.card.command.port.inbound.AuthorizeCardPaymentUseCase;
import com.example.ledgercore.card.command.port.inbound.CreateCreditCardUseCase;
import com.example.ledgercore.card.command.port.inbound.CreateDebitCardUseCase;
import com.example.ledgercore.card.query.dto.*;
import com.example.ledgercore.card.query.port.inbound.GetCardByIdUseCase;
import com.example.ledgercore.card.query.port.inbound.GetUserCardsUseCase;
import com.example.ledgercore.card.query.port.inbound.RevealCardDetailsUseCase;
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
    private final GetCardByIdUseCase getCardByIdUseCase;
    private final RevealCardDetailsUseCase revealCardDetailsUseCase;

    private final AuthorizeCardPaymentUseCase authorizeCardPaymentUseCase;
    private final AuthorizeCardPaymentByTokenUseCase
            authorizeCardPaymentByTokenUseCase;

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

    @GetMapping("/{cardId}")
    @Operation(
            summary = "Get card by ID",
            description = "Get a card owned by the authenticated customer"
    )
    public ResponseEntity<ApiResponse<CardInfo>> getCardById(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID cardId
    ) {
        CardInfo response =
                getCardByIdUseCase.execute(
                        new GetCardByIdQuery(
                                principal.getUserId(),
                                cardId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Card retrieved successfully"
                )
        );
    }

    @PostMapping("/{cardId}/reveal")
    @Operation(
            summary = "Reveal card details",
            description = "Reveal full card PAN and CVV after PIN verification"
    )
    public ResponseEntity<ApiResponse<RevealedCardDetails>> revealCardDetails(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID cardId,
            @RequestBody RevealCardDetailsRequest request
    ) {
        RevealedCardDetails result =
                revealCardDetailsUseCase.execute(
                        new RevealCardDetailsQuery(
                                principal.getUserId(),
                                cardId,
                                request.pin()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Card details revealed successfully"
                )
        );
    }

    @PostMapping("/authorizations")
    @Operation(
            summary = "Authorize card payment",
            description = "Authorize a card payment using card payment credentials"
    )
    public ResponseEntity<ApiResponse<AuthorizeCardPaymentResult>> authorizeCardPayment(
            @RequestHeader("X-Provider-Client-Id") String clientId,
            @RequestHeader("X-Provider-Credential") String credential,
            @Valid @RequestBody AuthorizeCardPaymentRequest request
    ) {
        AuthorizeCardPaymentResult result =
                authorizeCardPaymentUseCase.execute(
                        new AuthorizeCardPaymentCommand(
                                clientId,
                                credential,
                                request.reference(),
                                request.pan(),
                                request.expiryMonth(),
                                request.expiryYear(),
                                request.cvv(),
                                request.merchantReference(),
                                request.amount(),
                                request.currency()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Card payment authorized successfully"
                )
        );
    }

    @PostMapping("/authorizations/token")
    @Operation(
            summary = "Authorize card payment by token",
            description = "Authorize a card payment using a payment token"
    )
    public ResponseEntity<ApiResponse<AuthorizeCardPaymentByTokenResult>> authorizeCardPaymentByToken(
            @RequestHeader("X-Provider-Client-Id") String clientId,
            @RequestHeader("X-Provider-Credential") String credential,
            @Valid @RequestBody AuthorizeCardPaymentByTokenRequest request
    ) {
        AuthorizeCardPaymentByTokenResult result =
                authorizeCardPaymentByTokenUseCase.execute(
                        new AuthorizeCardPaymentByTokenCommand(
                                clientId,
                                credential,
                                request.token(),
                                request.reference(),
                                request.merchantReference(),
                                request.amount(),
                                request.currency()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Card payment authorized successfully"
                )
        );
    }
}