package com.example.ledgercore.businessday.adapter.inbound.web;

import com.example.ledgercore.businessday.command.port.inbound.CloseBusinessDayUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/business-days")
@RequiredArgsConstructor
public class BusinessDayController {

    private final CloseBusinessDayUseCase closeBusinessDayUseCase;

    @PostMapping("/close")
    public ResponseEntity<Void> closeBusinessDay() {

        closeBusinessDayUseCase.execute();

        return ResponseEntity.noContent().build();
    }
}