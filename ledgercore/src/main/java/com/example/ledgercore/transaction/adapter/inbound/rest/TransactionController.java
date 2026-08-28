package com.example.ledgercore.transaction.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.transaction.adapter.inbound.rest.dto.TransactionFilterRequest;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentResult;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.CreateTransferIntentUseCase;
import com.example.ledgercore.transaction.command.port.inbound.TransferMoneyUseCase;
import com.example.ledgercore.transaction.command.port.inbound.WithdrawMoneyUseCase;
import com.example.ledgercore.transaction.query.dto.GetAccountTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.GetTransactionByReferenceQuery;
import com.example.ledgercore.transaction.query.dto.GetTransactionQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.port.inbound.GetAccountTransactionsUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionByReferenceUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionUseCase;
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

    private final CreateTransferIntentUseCase
            createTransferIntentUseCase;

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;

    private final GetTransactionUseCase getTransactionUseCase;
    private final GetTransactionByReferenceUseCase
            getTransactionByReferenceUseCase;
    private final GetAccountTransactionsUseCase
            getAccountTransactionsUseCase;

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


    @PostMapping("/transfers")
    @Operation(
            summary = "Transfer money",
            description = "Transfer money from the authenticated user's account to another account"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody TransferMoneyCommand command
    ) {
        TransactionResponse response =
                transferMoneyUseCase.execute(
                        principal.getUserId(),
                        command
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Money transferred successfully"
                )
        );
    }

    @PostMapping("/withdraw")
    @Operation(
            summary = "Withdraw money",
            description = "Withdraw money from the authenticated user's account"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody WithdrawMoneyCommand command
    ) {
        TransactionResponse response =
                withdrawMoneyUseCase.execute(
                        principal.getUserId(),
                        command
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Money withdrawn successfully"
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

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(
            summary = "Get account transactions",
            description = "Get paginated transactions belonging to an account"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<TransactionResponse>>
            > getAccountTransactions(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID accountId,
            @ModelAttribute TransactionFilterRequest request
    ) {
        PageResponse<TransactionResponse> response =
                getAccountTransactionsUseCase.execute(
                        new GetAccountTransactionsQuery(
                                principal.getUserId(),
                                accountId,
                                request.getStatus(),
                                request.getType(),
                                request.getCurrency(),
                                request.getFrom(),
                                request.getTo(),
                                request.getPage(),
                                request.getSize()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Account transactions retrieved successfully"
                )
        );
    }
}