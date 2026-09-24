package com.example.ledgercore.cardtoken.adapter.inbound.rest;

import com.example.ledgercore.cardtoken.adapter.inbound.rest.dto.ProvisionCardTokenRequest;
import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenCommand;
import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenResult;
import com.example.ledgercore.cardtoken.command.dto.RevokeCardTokenCommand;
import com.example.ledgercore.cardtoken.command.dto.ResumeCardTokenCommand;
import com.example.ledgercore.cardtoken.command.dto.SuspendCardTokenCommand;
import com.example.ledgercore.cardtoken.command.port.inbound.ProvisionCardTokenUseCase;
import com.example.ledgercore.cardtoken.command.port.inbound.RevokeCardTokenUseCase;
import com.example.ledgercore.cardtoken.command.port.inbound.ResumeCardTokenUseCase;
import com.example.ledgercore.cardtoken.command.port.inbound.SuspendCardTokenUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provider/card-tokens")
@RequiredArgsConstructor
@Tag(
        name = "Provider Card Token",
        description = "Payment provider card token APIs"
)
public class ProviderCardTokenController {

    private final ProvisionCardTokenUseCase provisionCardTokenUseCase;
    private final SuspendCardTokenUseCase suspendCardTokenUseCase;
    private final ResumeCardTokenUseCase resumeCardTokenUseCase;
    private final RevokeCardTokenUseCase revokeCardTokenUseCase;

    @PostMapping
    @Operation(
            summary = "Provision card token",
            description = "Provision a token for a card authenticated by the payment provider"
    )
    public ResponseEntity<ApiResponse<ProvisionCardTokenResult>> provision(
            @RequestHeader("X-Provider-Client-Id")
            String clientId,

            @RequestHeader("X-Provider-Credential")
            String credential,

            @RequestBody
            ProvisionCardTokenRequest request
    ) {
        ProvisionCardTokenResult response =
                provisionCardTokenUseCase.execute(
                        new ProvisionCardTokenCommand(
                                clientId,
                                credential,
                                request.pan(),
                                request.expiryMonth(),
                                request.expiryYear(),
                                request.cvv(),
                                request.providerCustomerReference()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Card token provisioned successfully"
                )
        );
    }

    @PostMapping("/suspend")
    @Operation(
            summary = "Suspend card token",
            description = "Suspend an active card token"
    )
    public ResponseEntity<ApiResponse<Void>> suspend(
            @RequestHeader("X-Provider-Client-Id")
            String clientId,

            @RequestHeader("X-Provider-Credential")
            String credential,

            @RequestParam
            String token
    ) {
        suspendCardTokenUseCase.execute(
                new SuspendCardTokenCommand(
                        clientId,
                        credential,
                        token
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Card token suspended successfully"
                )
        );
    }

    @PostMapping("/resume")
    @Operation(
            summary = "Resume card token",
            description = "Resume a suspended card token"
    )
    public ResponseEntity<ApiResponse<Void>> resume(
            @RequestHeader("X-Provider-Client-Id")
            String clientId,

            @RequestHeader("X-Provider-Credential")
            String credential,

            @RequestParam
            String token
    ) {
        resumeCardTokenUseCase.execute(
                new ResumeCardTokenCommand(
                        clientId,
                        credential,
                        token
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Card token resumed successfully"
                )
        );
    }

    @PostMapping("/revoke")
    @Operation(
            summary = "Revoke card token",
            description = "Revoke a card token permanently"
    )
    public ResponseEntity<ApiResponse<Void>> revoke(
            @RequestHeader("X-Provider-Client-Id")
            String clientId,

            @RequestHeader("X-Provider-Credential")
            String credential,

            @RequestParam
            String token
    ) {
        revokeCardTokenUseCase.execute(
                new RevokeCardTokenCommand(
                        clientId,
                        credential,
                        token
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Card token revoked successfully"
                )
        );
    }
}