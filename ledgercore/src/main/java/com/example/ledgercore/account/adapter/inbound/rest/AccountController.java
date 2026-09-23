package com.example.ledgercore.account.adapter.inbound.rest;

import com.example.ledgercore.account.command.dto.ActivateUserAccountCommand;
import com.example.ledgercore.account.command.dto.BlockUserAccountCommand;
import com.example.ledgercore.account.command.dto.CloseUserAccountCommand;
import com.example.ledgercore.account.command.dto.CreatUserAccountCommand;
import com.example.ledgercore.account.command.port.inbound.ActivateUserAccountUseCase;
import com.example.ledgercore.account.command.port.inbound.BlockUserAccountUseCase;
import com.example.ledgercore.account.command.port.inbound.CloseUserAccountUseCase;
import com.example.ledgercore.account.command.port.inbound.CreateUserAccountUseCase;
import com.example.ledgercore.account.query.dto.*;
import com.example.ledgercore.account.query.port.inbound.GetAccountHolderUseCase;
import com.example.ledgercore.account.query.port.inbound.GetUserAccountUseCase;
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

    private final CreateUserAccountUseCase createUserAccountUseCase;
    private final BlockUserAccountUseCase blockUserAccountUseCase;
    private final ActivateUserAccountUseCase activateUserAccountUseCase;
    private final CloseUserAccountUseCase closeUserAccountUseCase;

    private final GetUserAccountUseCase getUserAccountUseCase;
    private final GetUserActiveAccountsUseCase getUserActiveAccountsUseCase;
    private final GetAccountHolderUseCase getAccountHolderUseCase;

    @PostMapping
    @Operation(
            summary = "Create account",
            description = "Create a new bank account for the authenticated user"
    )
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreatUserAccountCommand command
    ) {
        CreatUserAccountCommand actualCommand =
                new CreatUserAccountCommand(
                        principal.getUserId(),
                        command.productId(),
                        command.currency()
                );

        AccountResponse response =
                createUserAccountUseCase.execute(
                        actualCommand
                );

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
                getUserAccountUseCase.execute(
                        new GetUserAccountQuery(
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

    @GetMapping("/number/{accountNo}/holder")
    @Operation(
            summary = "Get account holder",
            description = "Get the full name of the account holder by account number"
    )
    public ResponseEntity<ApiResponse<AccountHolderResponse>> getAccountHolder(
            @PathVariable String accountNo
    ) {
        AccountHolderResponse response =
                getAccountHolderUseCase.execute(
                        new GetAccountHolderQuery(
                                accountNo
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Account holder retrieved successfully"
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
        blockUserAccountUseCase.execute(
                new BlockUserAccountCommand(
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
        activateUserAccountUseCase.execute(
                new ActivateUserAccountCommand(
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
        closeUserAccountUseCase.execute(
                new CloseUserAccountCommand(
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