package com.example.ledgercore.businessday.adapter.inbound.web;

import com.example.ledgercore.businessday.command.port.inbound.CloseBusinessDayUseCase;
import com.example.ledgercore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/business-days")
@RequiredArgsConstructor
public class BusinessDayController {

    private final CloseBusinessDayUseCase closeBusinessDayUseCase;

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