package com.example.ledgercore.transfer.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transfer.command.dto.ConfirmTransferCommand;
import com.example.ledgercore.transfer.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transfer.command.dto.CreateTransferIntentResult;
import com.example.ledgercore.transfer.command.port.inbound.ConfirmTransferUseCase;
import com.example.ledgercore.transfer.command.port.inbound.CreateTransferIntentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(
        name = "Transfers",
        description = "Money transfer APIs"
)
public class TransferController {

    private final CreateTransferIntentUseCase
            createTransferIntentUseCase;

    private final ConfirmTransferUseCase
            confirmTransferUseCase;

    @PostMapping("/transfer-intents")
    @Operation(
            summary = "Create transfer intent",
            description = """
                    Creates a money transfer intent and sends a confirmation OTP
                    to the authenticated user. The transfer is not executed until
                    the intent is confirmed with a valid OTP.
                    """
    )
    public ResponseEntity<ApiResponse<CreateTransferIntentResult>>
    createTransferIntent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateTransferIntentCommand command
    ) {
        CreateTransferIntentResult response =
                createTransferIntentUseCase.execute(
                        principal.getUserId(),
                        command
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transfer intent created successfully"
                )
        );
    }

    @PostMapping("/transfer-intents/confirm")
    @Operation(
            summary = "Confirm transfer intent",
            description = """
                    Confirms a transfer intent using the OTP sent to the
                    authenticated user's email. If the OTP is valid and the
                    intent is still pending and not expired, the transfer
                    will be executed.
                    """
    )
    public ResponseEntity<ApiResponse<TransactionResponse>>
    confirmTransfer(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ConfirmTransferCommand command
    ) {
        TransactionResponse response =
                confirmTransferUseCase.execute(
                        principal.getUserId(),
                        command
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transfer confirmed successfully"
                )
        );
    }
}