package com.example.ledgercore.credit.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferResult;
import com.example.ledgercore.credit.command.port.inbound.RejectCreditOfferUseCase;
import com.example.ledgercore.credit.query.dto.CreditOfferInfo;
import com.example.ledgercore.credit.query.dto.GetLatestCreditOfferQuery;
import com.example.ledgercore.credit.query.port.inbound.GetLatestCreditOfferUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    private final RejectCreditOfferUseCase
            rejectCreditOfferUseCase;

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

    @PostMapping("/{offerId}/reject")
    @Operation(
            summary = "Reject credit offer",
            description = """
                Reject an available credit offer
                for the currently authenticated customer
                """
    )
    public ResponseEntity<ApiResponse<RejectCreditOfferResult>> rejectOffer(
            @PathVariable UUID offerId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        RejectCreditOfferResult response =
                rejectCreditOfferUseCase.execute(
                        new RejectCreditOfferCommand(
                                offerId,
                                principal.getUserId()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Credit offer rejected successfully"
                )
        );
    }
}