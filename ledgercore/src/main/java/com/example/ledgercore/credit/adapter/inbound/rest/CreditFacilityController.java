package com.example.ledgercore.credit.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityResult;
import com.example.ledgercore.credit.query.port.inbound.GetUserCreditFacilityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/credit/facilities")
@RequiredArgsConstructor
@Tag(
        name = "Credit Facility",
        description = "Credit facility APIs"
)
public class CreditFacilityController {

    private final GetUserCreditFacilityUseCase getUserCreditFacilityUseCase;

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
}