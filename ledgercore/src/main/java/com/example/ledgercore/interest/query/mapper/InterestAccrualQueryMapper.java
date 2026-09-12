package com.example.ledgercore.interest.query.mapper;

import com.example.ledgercore.interest.entity.InterestAccrual;
import com.example.ledgercore.interest.query.dto.InterestAccrualResponse;

public final class InterestAccrualQueryMapper {

    private InterestAccrualQueryMapper() {
    }

    public static InterestAccrualResponse toResponse(
            InterestAccrual accrual
    ) {
        return new InterestAccrualResponse(
                accrual.getId(),
                accrual.getAccountId(),
                accrual.getCurrency(),
                accrual.getBusinessDate(),
                accrual.getInterestConfigId(),
                accrual.getPrincipalAmount(),
                accrual.getInterestRate(),
                accrual.getInterestAmount(),
                accrual.getJournalEntryId(),
                accrual.getPostingId(),
                accrual.getRunId(),
                accrual.getCreatedAt()
        );
    }
}