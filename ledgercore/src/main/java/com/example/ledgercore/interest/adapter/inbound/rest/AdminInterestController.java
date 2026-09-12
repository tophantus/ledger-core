package com.example.ledgercore.interest.adapter.inbound.rest;

import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.interest.enums.InterestRunStatus;
import com.example.ledgercore.interest.enums.InterestRunType;
import com.example.ledgercore.interest.query.dto.GetAdminInterestAccrualsQuery;
import com.example.ledgercore.interest.query.dto.GetAdminInterestPostingsQuery;
import com.example.ledgercore.interest.query.dto.GetAdminInterestRunsQuery;
import com.example.ledgercore.interest.query.dto.InterestAccrualResponse;
import com.example.ledgercore.interest.query.dto.InterestPostingResponse;
import com.example.ledgercore.interest.query.dto.InterestRunResponse;
import com.example.ledgercore.interest.query.port.inbound.GetAdminInterestAccrualsUseCase;
import com.example.ledgercore.interest.query.port.inbound.GetAdminInterestPostingsUseCase;
import com.example.ledgercore.interest.query.port.inbound.GetAdminInterestRunsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/interest")
@RequiredArgsConstructor
@Tag(
        name = "Admin Interest",
        description = "Administrative interest APIs"
)
public class AdminInterestController {

    private final GetAdminInterestRunsUseCase
            getAdminInterestRunsUseCase;

    private final GetAdminInterestAccrualsUseCase
            getAdminInterestAccrualsUseCase;

    private final GetAdminInterestPostingsUseCase
            getAdminInterestPostingsUseCase;

    @GetMapping("/runs")
    @Operation(
            summary = "Get interest runs",
            description = "Get paginated interest runs with optional filters"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<InterestRunResponse>>
            > getRuns(
            @RequestParam(required = false)
            LocalDate businessDate,

            @RequestParam(required = false)
            LocalDate fromDate,

            @RequestParam(required = false)
            LocalDate toDate,

            @RequestParam(required = false)
            InterestRunType runType,

            @RequestParam(required = false)
            InterestRunStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        GetAdminInterestRunsQuery query =
                new GetAdminInterestRunsQuery(
                        businessDate,
                        fromDate,
                        toDate,
                        runType,
                        status,
                        page,
                        size
                );

        PageResponse<InterestRunResponse> response =
                getAdminInterestRunsUseCase.execute(query);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Interest runs retrieved successfully"
                )
        );
    }

    @GetMapping("/accruals")
    @Operation(
            summary = "Get interest accruals",
            description = "Get paginated interest accruals. " +
                    "At least runId or businessDate must be provided"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<InterestAccrualResponse>>
            > getAccruals(
            @RequestParam(required = false)
            UUID runId,

            @RequestParam(required = false)
            LocalDate businessDate,

            @RequestParam(required = false)
            UUID accountId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        GetAdminInterestAccrualsQuery query =
                new GetAdminInterestAccrualsQuery(
                        runId,
                        businessDate,
                        accountId,
                        page,
                        size
                );

        PageResponse<InterestAccrualResponse> response =
                getAdminInterestAccrualsUseCase.execute(query);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Interest accruals retrieved successfully"
                )
        );
    }

    @GetMapping("/postings")
    @Operation(
            summary = "Get interest postings",
            description = "Get paginated interest postings. " +
                    "At least runId or businessDate must be provided"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<InterestPostingResponse>>
            > getPostings(
            @RequestParam(required = false)
            UUID runId,

            @RequestParam(required = false)
            LocalDate businessDate,

            @RequestParam(required = false)
            UUID accountId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        GetAdminInterestPostingsQuery query =
                new GetAdminInterestPostingsQuery(
                        runId,
                        businessDate,
                        accountId,
                        page,
                        size
                );

        PageResponse<InterestPostingResponse> response =
                getAdminInterestPostingsUseCase.execute(query);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Interest postings retrieved successfully"
                )
        );
    }
}