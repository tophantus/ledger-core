package com.example.ledgercore.transaction.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferMoneyUseCase;
import com.example.ledgercore.transaction.command.port.inbound.WithdrawMoneyUseCase;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
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
        name = "Transactions",
        description = "Money transaction APIs"
)
public class TransactionController {

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;

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
}