package com.example.ledgercore.account.adapter.inbound.rest;

import com.example.ledgercore.account.query.dto.*;
import com.example.ledgercore.account.query.port.inbound.GetProviderAccountByCredentialUseCase;
import com.example.ledgercore.account.query.port.inbound.GetProviderAccountsUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/provider/accounts")
@RequiredArgsConstructor
@Tag(
        name = "Provider Account",
        description = "Payment provider account APIs"
)
public class AccountProviderController {

    private final GetProviderAccountsUseCase
            getProviderAccountsUseCase;

    private final GetProviderAccountByCredentialUseCase
            getProviderAccountByCredentialUseCase;

    @GetMapping
    @Operation(
            summary = "Get provider accounts",
            description = "Get all accounts of the authenticated payment provider"
    )
    public ResponseEntity<ApiResponse<List<AccountSummaryResponse>>> getProviderAccounts(
            @RequestHeader("X-Provider-Client-Id") String clientId,
            @RequestHeader("X-Provider-Credential") String credential
    ) {
        List<AccountSummaryResponse> response =
                getProviderAccountsUseCase.execute(
                        new GetProviderAccountsQuery(
                                clientId,
                                credential
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Provider accounts retrieved successfully"
                )
        );
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "Get provider account by ID",
            description = "Get an account belonging to the authenticated payment provider"
    )
    public ResponseEntity<ApiResponse<AccountResponse>> getProviderAccount(
            @RequestHeader("X-Provider-Client-Id") String clientId,
            @RequestHeader("X-Provider-Credential") String credential,
            @PathVariable UUID accountId
    ) {
        AccountResponse response =
                getProviderAccountByCredentialUseCase.execute(
                        new GetProviderAccountByCredentialQuery(
                                clientId,
                                credential,
                                accountId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Provider account retrieved successfully"
                )
        );
    }
}