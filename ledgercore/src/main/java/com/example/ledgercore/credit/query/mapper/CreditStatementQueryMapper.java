package com.example.ledgercore.credit.query.mapper;

import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.query.dto.GetCreditStatementResponse;

public final class CreditStatementQueryMapper {

    private CreditStatementQueryMapper() {
    }

    public static GetCreditStatementResponse toResponse(
            CreditStatement statement
    ) {
        return new GetCreditStatementResponse(
                statement.getId(),
                statement.getCreditFacilityId(),
                statement.getPeriodStart(),
                statement.getPeriodEnd(),
                statement.getStatementDate(),
                statement.getDueDate(),
                statement.getOpeningBalance().toPlainString(),
                statement.getPurchasesAmount().toPlainString(),
                statement.getPaymentsAmount().toPlainString(),
                statement.getFeesAmount().toPlainString(),
                statement.getInterestAmount().toPlainString(),
                statement.getPostedInterestAmount().toPlainString(),
                statement.getClosingBalance().toPlainString(),
                statement.getMinimumPayment().toPlainString(),
                statement.getPaidAmount().toPlainString(),
                statement.getStatus()
        );
    }
}