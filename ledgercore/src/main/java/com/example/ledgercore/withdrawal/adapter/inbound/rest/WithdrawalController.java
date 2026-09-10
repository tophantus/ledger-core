package com.example.ledgercore.withdrawal.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.withdrawal.adapter.inbound.rest.dto.ConfirmWithdrawalRequest;
import com.example.ledgercore.withdrawal.adapter.inbound.rest.dto.CreateWithdrawalRequest;
import com.example.ledgercore.withdrawal.adapter.inbound.rest.dto.ExecuteWithdrawalRequest;
import com.example.ledgercore.withdrawal.command.dto.*;
import com.example.ledgercore.withdrawal.command.port.inbound.CancelWithdrawalIntentUseCase;
import com.example.ledgercore.withdrawal.command.port.inbound.ConfirmWithdrawalRequestUseCase;
import com.example.ledgercore.withdrawal.command.port.inbound.CreateWithdrawalRequestUseCase;
import com.example.ledgercore.withdrawal.command.port.inbound.ExecuteWithdrawalUseCase;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import com.example.ledgercore.withdrawal.query.dto.GetAccountWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.GetUserWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;
import com.example.ledgercore.withdrawal.query.port.inbound.GetAccountWithdrawalIntentsUseCase;
import com.example.ledgercore.withdrawal.query.port.inbound.GetUserWithdrawalIntentsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/withdrawals")
@RequiredArgsConstructor
@Tag(
        name = "Withdrawal",
        description = "Withdrawal management APIs"
)
public class WithdrawalController {

    private final CreateWithdrawalRequestUseCase
            createWithdrawalRequestUseCase;

    private final ConfirmWithdrawalRequestUseCase
            confirmWithdrawalRequestUseCase;

    private final ExecuteWithdrawalUseCase
            executeWithdrawalUseCase;

    private final GetAccountWithdrawalIntentsUseCase
            getAccountWithdrawalIntentsUseCase;

    private final GetUserWithdrawalIntentsUseCase
            getUserWithdrawalIntentsUseCase;

    private final CancelWithdrawalIntentUseCase
            cancelWithdrawalIntentUseCase;

    @PostMapping("/requests")
    @Operation(
            summary = "Create withdrawal request",
            description = "Create a new withdrawal request for the authenticated user"
    )
    public ResponseEntity<ApiResponse<WithdrawalRequestResponse>> createRequest(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateWithdrawalRequest request
    ) {
        WithdrawalRequestResponse response =
                createWithdrawalRequestUseCase.execute(
                        new CreateWithdrawalRequestCommand(
                                principal.getUserId(),
                                request.accountId(),
                                request.amount(),
                                request.currency()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Withdrawal request created successfully"
                )
        );
    }

    @PostMapping("/requests/{requestId}/confirm")
    @Operation(
            summary = "Confirm withdrawal request",
            description = "Confirm a withdrawal request using the verification OTP"
    )
    public ResponseEntity<ApiResponse<ConfirmWithdrawalRequestResponse>> confirmRequest(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requestId,
            @Valid @RequestBody ConfirmWithdrawalRequest request
    ) {
        ConfirmWithdrawalRequestResponse response =
                confirmWithdrawalRequestUseCase.execute(
                        new ConfirmWithdrawalRequestCommand(
                                principal.getUserId(),
                                requestId,
                                request.otp()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Withdrawal request confirmed successfully"
                )
        );
    }

    @PostMapping("/execute")
    @Operation(
            summary = "Execute withdrawal",
            description = "Execute a confirmed withdrawal intent through an authenticated ATM terminal"
    )
    public ResponseEntity<ApiResponse<ExecuteWithdrawalResponse>> executeWithdrawal(
            @RequestHeader("X-ATM-Terminal") String terminalCode,
            @RequestHeader("X-ATM-Credential") String credential,
            @Valid @RequestBody ExecuteWithdrawalRequest request
    ) {
        ExecuteWithdrawalResponse response =
                executeWithdrawalUseCase.execute(
                        new ExecuteWithdrawalCommand(
                                terminalCode,
                                credential,
                                request.lookupCode(),
                                request.withdrawalCode(),
                                request.amount()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Withdrawal executed successfully"
                )
        );
    }

    @GetMapping("/intents")
    @Operation(
            summary = "Get withdrawal intents",
            description = "Get withdrawal intents for the authenticated user, optionally filtered by account"
    )
    public ResponseEntity<ApiResponse<PageResponse<WithdrawalIntentResponse>>> getWithdrawalIntents(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) WithdrawalIntentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<WithdrawalIntentResponse> response;

        if (accountId != null) {
            response =
                    getAccountWithdrawalIntentsUseCase.execute(
                            new GetAccountWithdrawalIntentsQuery(
                                    principal.getUserId(),
                                    accountId,
                                    status,
                                    page,
                                    size
                            )
                    );
        } else {
            response =
                    getUserWithdrawalIntentsUseCase.execute(
                            new GetUserWithdrawalIntentsQuery(
                                    principal.getUserId(),
                                    status,
                                    page,
                                    size
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Withdrawal intents retrieved successfully"
                )
        );
    }

    @PostMapping("/intents/{intentId}/cancel")
    @Operation(
            summary = "Cancel withdrawal intent",
            description = "Cancel a ready withdrawal intent and release its hold"
    )
    public ResponseEntity<ApiResponse<Void>> cancelWithdrawalIntent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID intentId
    ) {
        cancelWithdrawalIntentUseCase.execute(
                new CancelWithdrawalIntentCommand(
                        principal.getUserId(),
                        intentId
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Withdrawal intent cancelled successfully"
                )
        );
    }
}