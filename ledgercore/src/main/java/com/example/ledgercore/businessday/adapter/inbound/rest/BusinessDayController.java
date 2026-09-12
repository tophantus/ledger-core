package com.example.ledgercore.businessday.adapter.inbound.rest;

import com.example.ledgercore.businessday.command.port.inbound.CloseBusinessDayUseCase;
import com.example.ledgercore.businessday.query.dto.CurrentBusinessDayResponse;
import com.example.ledgercore.businessday.query.port.inbound.GetAdminCurrentBusinessDayUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/business-days")
@RequiredArgsConstructor
@Tag(
        name = "Admin Business Days",
        description = "Administrative business day APIs"
)
public class BusinessDayController {

    private final GetAdminCurrentBusinessDayUseCase
            getCurrentBusinessDayUseCase;

    private final CloseBusinessDayUseCase closeBusinessDayUseCase;

    @GetMapping("/current")
    @Operation(
            summary = "Get current business day",
            description = "Get the current business day and its status"
    )
    public ResponseEntity<ApiResponse<CurrentBusinessDayResponse>>
    getCurrentBusinessDay() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        getCurrentBusinessDayUseCase.execute(),
                        "Current business day retrieved successfully"
                )
        );
    }

    @PostMapping("/close")
    @Operation(
            summary = "Close business day",
            description = "Close the current business day"
    )
    public ResponseEntity<ApiResponse<Void>> closeBusinessDay() {

        closeBusinessDayUseCase.execute();

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Business day closed successfully"
                )
        );
    }
}