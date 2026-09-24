package com.example.ledgercore.transaction.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.transaction.adapter.inbound.rest.dto.TransactionFilterRequest;
import com.example.ledgercore.transaction.query.dto.*;
import com.example.ledgercore.transaction.query.port.inbound.GetAccountTransactionsUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionByReferenceUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetUserTransactionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final GetTransactionUseCase getTransactionUseCase;
    private final GetTransactionByReferenceUseCase
            getTransactionByReferenceUseCase;
    private final GetAccountTransactionsUseCase
            getAccountTransactionsUseCase;

    private final GetUserTransactionsUseCase
            getUserTransactionsUseCase;

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