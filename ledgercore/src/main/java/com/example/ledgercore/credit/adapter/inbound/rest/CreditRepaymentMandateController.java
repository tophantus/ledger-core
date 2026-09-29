package com.example.ledgercore.credit.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.credit.adapter.inbound.rest.dto.CreateCreditRepaymentMandateRequest;
import com.example.ledgercore.credit.adapter.inbound.rest.dto.UpdateCreditRepaymentMandateRequest;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.port.inbound.CreateCreditRepaymentMandateUseCase;
import com.example.ledgercore.credit.command.port.inbound.RevokeCreditRepaymentMandateUseCase;
import com.example.ledgercore.credit.command.port.inbound.UpdateCreditRepaymentMandateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit/repayment-mandates")
@RequiredArgsConstructor
@Tag(
        name = "Credit Repayment Mandate",
        description = "Credit repayment mandate APIs"
)
public class CreditRepaymentMandateController {

    private final CreateCreditRepaymentMandateUseCase createUseCase;
    private final UpdateCreditRepaymentMandateUseCase updateUseCase;
    private final RevokeCreditRepaymentMandateUseCase revokeUseCase;

    @PostMapping
    @Operation(summary = "Create credit repayment mandate")
    public ResponseEntity<ApiResponse<CreateCreditRepaymentMandateResult>> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody CreateCreditRepaymentMandateRequest request
    ) {
        CreateCreditRepaymentMandateResult result =
                createUseCase.execute(
                        new CreateCreditRepaymentMandateCommand(
                                principal.getUserId(),
                                request.creditFacilityId(),
                                request.accountId(),
                                request.repaymentType()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        result,
                        "Credit repayment mandate created successfully"
                ));
    }

    @PutMapping("/{mandateId}")
    @Operation(summary = "Update credit repayment mandate")
    public ResponseEntity<ApiResponse<UpdateCreditRepaymentMandateResult>> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID mandateId,
            @RequestBody UpdateCreditRepaymentMandateRequest request
    ) {
        UpdateCreditRepaymentMandateResult result =
                updateUseCase.execute(
                        new UpdateCreditRepaymentMandateCommand(
                                principal.getUserId(),
                                mandateId,
                                request.repaymentType()
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Credit repayment mandate updated successfully"
                )
        );
    }

    @DeleteMapping("/{mandateId}")
    @Operation(summary = "Revoke credit repayment mandate")
    public ResponseEntity<ApiResponse<RevokeCreditRepaymentMandateResult>> revoke(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID mandateId
    ) {
        RevokeCreditRepaymentMandateResult result =
                revokeUseCase.execute(
                        new RevokeCreditRepaymentMandateCommand(
                                principal.getUserId(),
                                mandateId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        result,
                        "Credit repayment mandate revoked successfully"
                )
        );
    }
}