package com.example.ledgercore.interest.query.dto;

import com.example.ledgercore.interest.enums.InterestRunStatus;
import com.example.ledgercore.interest.enums.InterestRunType;

import java.time.LocalDate;

public record GetAdminInterestRunsQuery(
        LocalDate businessDate,
        LocalDate fromDate,
        LocalDate toDate,
        InterestRunType runType,
        InterestRunStatus status,
        int page,
        int size
) {
}