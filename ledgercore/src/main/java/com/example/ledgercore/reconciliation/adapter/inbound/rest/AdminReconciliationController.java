package com.example.ledgercore.reconciliation.adapter.inbound.rest;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationExceptionResponse;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationRunSummaryResponse;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationSummaryResponse;
import com.example.ledgercore.reconciliation.query.port.inbound.GetReconciliationExceptionsUseCase;
import com.example.ledgercore.reconciliation.query.port.inbound.GetReconciliationSummaryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reconciliation")
@RequiredArgsConstructor
@Tag(
        name = "Admin Reconciliation",
        description = "Administrative reconciliation APIs"
)
public class AdminReconciliationController {

    private final GetReconciliationSummaryUseCase
            getReconciliationSummaryUseCase;

    private final GetReconciliationExceptionsUseCase
            getReconciliationExceptionsUseCase;

    @GetMapping("/summary")
    @Operation(
            summary = "Get reconciliation summary",
            description = "Get reconciliation runs for a business date, "
                    + "or the latest available business date when omitted"
    )
    public ResponseEntity<
            ApiResponse<ReconciliationSummaryResponse>
            > getSummary(
            @RequestParam(required = false)
            LocalDate businessDate
    ) {

        ReconciliationSummaryResponse response =
                getReconciliationSummaryUseCase.execute(
                        businessDate
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Reconciliation summary retrieved successfully"
                )
        );
    }

    @GetMapping("/exceptions")
    @Operation(
            summary = "Get reconciliation exceptions",
            description = "Get reconciliation exceptions with pagination and optional filters"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<ReconciliationExceptionResponse>>
            > getExceptions(
            @RequestParam(required = false)
            LocalDate businessDate,

            @RequestParam(required = false)
            ReconciliationTargetType targetType,

            @RequestParam(required = false)
            ReconciliationErrorCode errorCode,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        PageResponse<ReconciliationExceptionResponse> response =
                getReconciliationExceptionsUseCase.execute(
                        businessDate,
                        targetType,
                        errorCode,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Reconciliation exceptions retrieved successfully"
                )
        );
    }
}