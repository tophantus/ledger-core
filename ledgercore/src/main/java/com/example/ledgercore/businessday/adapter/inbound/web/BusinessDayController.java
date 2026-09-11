package com.example.ledgercore.businessday.adapter.inbound.web;

import com.example.ledgercore.businessday.command.port.inbound.CloseBusinessDayUseCase;
import com.example.ledgercore.businessday.query.dto.CurrentBusinessDayResponse;
import com.example.ledgercore.businessday.query.port.inbound.GetAdminCurrentBusinessDayUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/business-days")
@RequiredArgsConstructor
public class BusinessDayController {

    private final GetAdminCurrentBusinessDayUseCase
            getCurrentBusinessDayUseCase;

    private final CloseBusinessDayUseCase closeBusinessDayUseCase;

    @GetMapping("/current")
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