package com.example.ledgercore.transaction.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.transaction.command.dto.DepositMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.DepositMoneyUseCase;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
@Tag(
        name = "Admin Transactions",
        description = "Administrative transaction APIs"
)
public class AdminTransactionController {

    private final DepositMoneyUseCase depositMoneyUseCase;

    @PostMapping("/deposits")
    @Operation(
            summary = "Deposit money",
            description = "Deposit money into a customer account"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody DepositMoneyCommand command
    ) {
        TransactionResponse response =
                depositMoneyUseCase.execute(
                        principal.getUserId(),
                        command
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Money deposited successfully"
                )
        );
    }
}