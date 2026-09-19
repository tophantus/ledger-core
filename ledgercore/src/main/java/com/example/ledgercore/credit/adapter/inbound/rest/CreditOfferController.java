package com.example.ledgercore.credit.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.credit.query.dto.CreditOfferInfo;
import com.example.ledgercore.credit.query.dto.GetLatestCreditOfferQuery;
import com.example.ledgercore.credit.query.port.inbound.GetLatestCreditOfferUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit/offers")
@RequiredArgsConstructor
@Tag(
        name = "Credit Offer",
        description = "Credit offer APIs"
)
public class CreditOfferController {

    private final GetLatestCreditOfferUseCase
            getLatestCreditOfferUseCase;

    @GetMapping("/latest")
    @Operation(
            summary = "Get latest credit offer",
            description = """
                    Get the latest active credit offer
                    available to the currently authenticated customer
                    """
    )
    public ResponseEntity<ApiResponse<CreditOfferInfo>> getLatestOffer(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return getLatestCreditOfferUseCase
                .execute(
                        new GetLatestCreditOfferQuery(
                                principal.getUserId()
                        )
                )
                .map(response ->
                        ResponseEntity.ok(
                                ApiResponse.success(
                                        response,
                                        "Latest credit offer retrieved successfully"
                                )
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.noContent().build()
                );
    }
}