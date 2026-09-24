package com.example.ledgercore.card.adapter.inbound.rest;

import com.example.ledgercore.card.adapter.inbound.rest.dto.AuthorizeCardPaymentByTokenRequest;
import com.example.ledgercore.card.adapter.inbound.rest.dto.AuthorizeCardPaymentRequest;
import com.example.ledgercore.card.adapter.inbound.rest.dto.CaptureCardPaymentRequest;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenResult;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentResult;
import com.example.ledgercore.card.command.dto.CaptureCardPaymentCommand;
import com.example.ledgercore.card.command.dto.CaptureCardPaymentResult;
import com.example.ledgercore.card.command.port.inbound.AuthorizeCardPaymentByTokenUseCase;
import com.example.ledgercore.card.command.port.inbound.AuthorizeCardPaymentUseCase;
import com.example.ledgercore.card.command.port.inbound.CaptureCardPaymentUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provider/cards")
@RequiredArgsConstructor
@Tag(
        name = "Provider Card",
        description = "Payment provider card payment APIs"
)
public class ProviderCardController {

    private final AuthorizeCardPaymentUseCase
            authorizeCardPaymentUseCase;

    private final AuthorizeCardPaymentByTokenUseCase
            authorizeCardPaymentByTokenUseCase;

    private final CaptureCardPaymentUseCase
            captureCardPaymentUseCase;

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

    @PostMapping("/captures")
    @Operation(
            summary = "Capture card authorization",
            description = "Capture an authorized card payment"
    )
    public ResponseEntity<ApiResponse<CaptureCardPaymentResult>> captureCardPayment(
            @RequestHeader("X-Provider-Client-Id") String clientId,
            @RequestHeader("X-Provider-Credential") String credential,
            @Valid @RequestBody CaptureCardPaymentRequest request
    ) {
        CaptureCardPaymentResult result =
                captureCardPaymentUseCase.execute(
                        new CaptureCardPaymentCommand(
                                clientId,
                                credential,
                                request.authorizationId(),
                                request.reference(),
                                request.description()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Card payment captured successfully"
                )
        );
    }
}