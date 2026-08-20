package com.example.ledgercore.account.adapter.inbound.rest;

import com.example.ledgercore.account.command.dto.ActivateAccountCommand;
import com.example.ledgercore.account.command.dto.BlockAccountCommand;
import com.example.ledgercore.account.command.dto.CloseAccountCommand;
import com.example.ledgercore.account.command.dto.CreateAccountCommand;
import com.example.ledgercore.account.command.port.inbound.ActivateAccountUseCase;
import com.example.ledgercore.account.command.port.inbound.BlockAccountUseCase;
import com.example.ledgercore.account.command.port.inbound.CloseAccountUseCase;
import com.example.ledgercore.account.command.port.inbound.CreateAccountUseCase;
import com.example.ledgercore.account.query.dto.*;
import com.example.ledgercore.account.query.port.inbound.GetAccountByAccountNoUseCase;
import com.example.ledgercore.account.query.port.inbound.GetAccountUseCase;
import com.example.ledgercore.account.query.port.inbound.GetUserActiveAccountsUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.ledgercore.auth.security.AuthPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(
        name = "Accounts",
        description = "Bank account management APIs"
)
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final BlockAccountUseCase blockAccountUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final CloseAccountUseCase closeAccountUseCase;

    private final GetAccountUseCase getAccountUseCase;
    private final GetAccountByAccountNoUseCase getAccountByAccountNoUseCase;
    private final GetUserActiveAccountsUseCase getUserActiveAccountsUseCase;

    @PostMapping
    @Operation(
            summary = "Create account",
            description = "Create a new bank account for the authenticated user"
    )
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateAccountCommand command
    ) {
        CreateAccountCommand actualCommand = new CreateAccountCommand(
                principal.getUserId(),
                command.currency()
        );

        AccountResponse response =
                createAccountUseCase.execute(actualCommand);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Account created successfully"
                )
        );
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "Get account",
            description = "Get an account by account ID"
    )
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID accountId
    ) {
        AccountResponse response =
                getAccountUseCase.execute(
                        new GetAccountQuery(
                                principal.getUserId(),
                                accountId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Account retrieved successfully"
                )
        );
    }

    @GetMapping("/number/{accountNo}")
    @Operation(
            summary = "Get account by account number",
            description = "Get an account by its account number"
    )
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByAccountNo(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String accountNo
    ) {
        AccountResponse response =
                getAccountByAccountNoUseCase.execute(
                        new GetAccountByAccountNoQuery(
                                principal.getUserId(),
                                accountNo
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Account retrieved successfully"
                )
        );
    }

    @GetMapping
    @Operation(
            summary = "Get my accounts",
            description = "Get all accounts belonging to the authenticated user"
    )
    public ResponseEntity<ApiResponse<List<AccountSummaryResponse>>> getMyAccounts(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        List<AccountSummaryResponse> response =
                getUserActiveAccountsUseCase.execute(
                        new GetActiveUserAccountsQuery(
                                principal.getUserId()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Accounts retrieved successfully"
                )
        );
    }

    @PostMapping("/{accountId}/block")
    @Operation(
            summary = "Block account",
            description = "Block an active bank account"
    )
    public ResponseEntity<ApiResponse<Void>> blockAccount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID accountId
    ) {
        blockAccountUseCase.execute(
                new BlockAccountCommand(
                        principal.getUserId(),
                        accountId
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Account blocked successfully"
                )
        );
    }

    @PostMapping("/{accountId}/activate")
    @Operation(
            summary = "Activate account",
            description = "Activate a blocked bank account"
    )
    public ResponseEntity<ApiResponse<Void>> activateAccount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID accountId
    ) {
        activateAccountUseCase.execute(
                new ActivateAccountCommand(
                        principal.getUserId(),
                        accountId
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Account activated successfully"
                )
        );
    }

    @PostMapping("/{accountId}/close")
    @Operation(
            summary = "Close account",
            description = "Close a bank account with zero balance"
    )
    public ResponseEntity<ApiResponse<Void>> closeAccount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID accountId
    ) {
        closeAccountUseCase.execute(
                new CloseAccountCommand(
                        principal.getUserId(),
                        accountId
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Account closed successfully"
                )
        );
    }
}