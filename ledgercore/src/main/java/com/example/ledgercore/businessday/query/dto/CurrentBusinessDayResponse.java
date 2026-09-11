package com.example.ledgercore.businessday.query.dto;

import com.example.ledgercore.businessday.enums.BusinessDayStatus;

import java.time.LocalDate;

public record CurrentBusinessDayResponse(
        LocalDate businessDate,
        BusinessDayStatus status,
        boolean canClose
) {
}