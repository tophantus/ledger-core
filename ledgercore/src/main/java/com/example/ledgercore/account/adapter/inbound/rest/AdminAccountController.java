package com.example.ledgercore.account.adapter.inbound.rest;

import com.example.ledgercore.account.adapter.inbound.rest.dto.AdminAccountFilterRequest;
import com.example.ledgercore.account.query.dto.AdminAccountResponse;
import com.example.ledgercore.account.query.dto.AdminAccountFilter;
import com.example.ledgercore.account.query.port.inbound.GetAdminAccountsUseCase;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@Tag(
        name = "Admin Accounts",
        description = "Administrative account management APIs"
)
public class AdminAccountController {

    private final GetAdminAccountsUseCase getAdminAccountsUseCase;

    @GetMapping
    @Operation(
            summary = "Get accounts",
            description = "Get paginated bank accounts with optional filters"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<AdminAccountResponse>>
            > getAccounts(
            @ModelAttribute AdminAccountFilterRequest request
    ) {
        PageResponse<AdminAccountResponse> response =
                getAdminAccountsUseCase.execute(
                        new AdminAccountFilter(
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
}