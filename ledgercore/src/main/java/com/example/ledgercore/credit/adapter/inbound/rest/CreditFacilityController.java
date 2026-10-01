package com.example.ledgercore.credit.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.credit.query.dto.GetCreditStatementResponse;
import com.example.ledgercore.credit.query.dto.GetCreditStatementsQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityResult;
import com.example.ledgercore.credit.query.port.inbound.GetCreditStatementsUseCase;
import com.example.ledgercore.credit.query.port.inbound.GetUserCreditFacilityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit/facilities")
@RequiredArgsConstructor
@Tag(
        name = "Credit Facility",
        description = "Credit facility APIs"
)
public class CreditFacilityController {

    private final GetUserCreditFacilityUseCase getUserCreditFacilityUseCase;
    private final GetCreditStatementsUseCase getCreditStatementsUseCase;

    @GetMapping
    @Operation(
            summary = "Get user credit facility",
            description = """
                    Get the active credit facility of the
                    currently authenticated user
                    """
    )
    public ResponseEntity<ApiResponse<GetUserCreditFacilityResult>>
    getUserCreditFacility(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        Optional<GetUserCreditFacilityResult> result =
                getUserCreditFacilityUseCase.execute(
                        new GetUserCreditFacilityQuery(
                                principal.getUserId()
                        )
                );

        return result
                .map(response -> ResponseEntity.ok(
                        ApiResponse.success(
                                response,
                                "User credit facility retrieved successfully"
                        )
                ))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{creditFacilityId}/statements")
    @Operation(
            summary = "Get credit facility statements",
            description = """
                    Get paginated statements of the specified credit facility
                    belonging to the currently authenticated user
                    """
    )
    public ResponseEntity<
            ApiResponse<PageResponse<GetCreditStatementResponse>>
            > getCreditStatements(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID creditFacilityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<GetCreditStatementResponse> response =
                getCreditStatementsUseCase.execute(
                        new GetCreditStatementsQuery(
                                principal.getUserId(),
                                creditFacilityId,
                                page,
                                size
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Credit statements retrieved successfully"
                )
        );
    }
}