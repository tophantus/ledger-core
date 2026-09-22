package com.example.ledgercore.provider.adapter.inbound.rest;

import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderResult;
import com.example.ledgercore.provider.command.port.inbound.RegisterPaymentProviderUseCase;
import com.example.ledgercore.provider.enums.ProviderType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Tag(
        name = "Payment Provider",
        description = "Payment provider management APIs"
)
public class PaymentProviderController {

    private final RegisterPaymentProviderUseCase
            registerPaymentProviderUseCase;

    @PostMapping
    @Operation(
            summary = "Register payment provider",
            description = "Register a new payment provider and generate provider credentials"
    )
    public ResponseEntity<ApiResponse<RegisterPaymentProviderResult>> register(
            @RequestBody RegisterPaymentProviderRequest request
    ) {
        RegisterPaymentProviderResult response =
                registerPaymentProviderUseCase.execute(
                        new RegisterPaymentProviderCommand(
                                request.code(),
                                request.name(),
                                request.type()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                response,
                                "Payment provider registered successfully"
                        )
                );
    }

    public record RegisterPaymentProviderRequest(
            String code,
            String name,
            ProviderType type
    ) {
    }
}