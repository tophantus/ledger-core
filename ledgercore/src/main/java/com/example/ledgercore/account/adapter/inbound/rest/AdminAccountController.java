package com.example.ledgercore.account.adapter.inbound.rest;

import com.example.ledgercore.account.adapter.inbound.rest.dto.AdminAccountFilterRequest;
import com.example.ledgercore.account.query.dto.AdminUserAccountDetailResponse;
import com.example.ledgercore.account.query.dto.AdminUserAccountFilter;
import com.example.ledgercore.account.query.dto.AdminUserAccountResponse;
import com.example.ledgercore.account.query.port.inbound.GetAdminAccountDetailUseCase;
import com.example.ledgercore.account.query.port.inbound.GetAdminUserAccountsUseCase;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@Tag(
        name = "Admin Accounts",
        description = "Administrative account management APIs"
)
public class AdminAccountController {

    private final GetAdminUserAccountsUseCase
            getAdminUserAccountsUseCase;

    private final GetAdminAccountDetailUseCase
            getAdminAccountDetailUseCase;

    @GetMapping
    @Operation(
            summary = "Get accounts",
            description = "Get paginated bank accounts with optional filters"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<AdminUserAccountResponse>>
            > getAccounts(
            @ModelAttribute AdminAccountFilterRequest request
    ) {

        PageResponse<AdminUserAccountResponse> response =
                getAdminUserAccountsUseCase.execute(
                        new AdminUserAccountFilter(
                                request.getAccountNo(),
                                request.getStatus(),
                                request.getCurrency(),
                                request.getUserId(),
                                request.getPage(),
                                request.getSize()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Accounts retrieved successfully"
                )
        );
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "Get account details",
            description = "Get account details including account owner information"
    )
    public ResponseEntity<
            ApiResponse<AdminUserAccountDetailResponse>
            > getAccount(
            @PathVariable UUID accountId
    ) {

        AdminUserAccountDetailResponse response =
                getAdminAccountDetailUseCase.execute(
                        accountId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Account details retrieved successfully"
                )
        );
    }
}