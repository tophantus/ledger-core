package com.example.ledgercore.transaction.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.transaction.adapter.inbound.rest.dto.TransactionFilterRequest;
import com.example.ledgercore.transaction.command.dto.*;
import com.example.ledgercore.transaction.command.port.inbound.ConfirmTransferUseCase;
import com.example.ledgercore.transaction.command.port.inbound.CreateTransferIntentUseCase;
import com.example.ledgercore.transaction.query.dto.*;
import com.example.ledgercore.transaction.query.port.inbound.GetAccountTransactionsUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionByReferenceUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetUserTransactionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(
        name = "Transactions",
        description = "Money transaction APIs"
)
public class TransactionController {

    private final CreateTransferIntentUseCase createTransferIntentUseCase;
    private final ConfirmTransferUseCase confirmTransferUseCase;

    private final GetTransactionUseCase getTransactionUseCase;
    private final GetTransactionByReferenceUseCase
            getTransactionByReferenceUseCase;
    private final GetAccountTransactionsUseCase
            getAccountTransactionsUseCase;

    private final GetUserTransactionsUseCase
            getUserTransactionsUseCase;

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

    @GetMapping("/{transactionId}")
    @Operation(
            summary = "Get transaction",
            description = "Get a transaction by its ID"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID transactionId
    ) {
        TransactionResponse response =
                getTransactionUseCase.execute(
                        new GetTransactionQuery(
                                principal.getUserId(),
                                transactionId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transaction retrieved successfully"
                )
        );
    }

    @GetMapping("/reference/{reference}")
    @Operation(
            summary = "Get transaction by reference",
            description = "Get a transaction by its reference"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>>
    getTransactionByReference(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String reference
    ) {
        TransactionResponse response =
                getTransactionByReferenceUseCase.execute(
                        new GetTransactionByReferenceQuery(
                                principal.getUserId(),
                                reference
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transaction retrieved successfully"
                )
        );
    }

    @GetMapping
    @Operation(
            summary = "Get transactions",
            description = "Get paginated transactions for the current user, optionally filtered by account"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<TransactionResponse>>
            > getTransactions(
            @AuthenticationPrincipal AuthPrincipal principal,
            @ModelAttribute TransactionFilterRequest request
    ) {
        PageResponse<TransactionResponse> response;

        if (request.getAccountId()!= null) {
            response =
                    getAccountTransactionsUseCase.execute(
                            new GetAccountTransactionsQuery(
                                    principal.getUserId(),
                                    request.getAccountId(),
                                    request.getStatus(),
                                    request.getType(),
                                    request.getCurrency(),
                                    request.getFrom(),
                                    request.getTo(),
                                    request.getPage(),
                                    request.getSize()
                            )
                    );
        } else {
            response =
                    getUserTransactionsUseCase.execute(
                            new GetUserTransactionsQuery(
                                    principal.getUserId(),
                                    request.getStatus(),
                                    request.getType(),
                                    request.getCurrency(),
                                    request.getFrom(),
                                    request.getTo(),
                                    request.getPage(),
                                    request.getSize()
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transactions retrieved successfully"
                )
        );
    }

}