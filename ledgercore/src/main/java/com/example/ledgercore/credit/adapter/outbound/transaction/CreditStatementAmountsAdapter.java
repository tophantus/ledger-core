package com.example.ledgercore.credit.adapter.outbound.transaction;

import com.example.ledgercore.credit.command.port.outbound.CreditStatementAmountsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditStatementAmounts;
import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalQuery;
import com.example.ledgercore.interest.query.port.inbound.GetCreditInterestAccrualTotalUseCase;
import com.example.ledgercore.transaction.query.dto.GetCreditFeeAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountQuery;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditFeeAmountUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditInterestAmountUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditPaymentAmountUseCase;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditPurchasesAmountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditStatementAmountsAdapter
        implements CreditStatementAmountsPort {

    private final GetCreditPurchasesAmountUseCase purchasesAmountUseCase;
    private final GetCreditPaymentAmountUseCase paymentsAmountUseCase;
    private final GetCreditFeeAmountUseCase feesAmountUseCase;
    private final GetCreditInterestAccrualTotalUseCase
            interestAccrualTotalUseCase;
    private final GetCreditInterestAmountUseCase interestAmountUseCase;

    @Override
    public CreditStatementAmounts getAmounts(
            UUID creditFacilityId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return new CreditStatementAmounts(
                purchasesAmountUseCase.execute(
                        new GetCreditPurchasesAmountQuery(
                                creditFacilityId, periodStart, periodEnd
                        )
                ).totalAmount(),
                paymentsAmountUseCase.execute(
                        new GetCreditPaymentAmountQuery(
                                creditFacilityId, periodStart, periodEnd
                        )
                ).totalAmount(),
                feesAmountUseCase.execute(
                        new GetCreditFeeAmountQuery(
                                creditFacilityId, periodStart, periodEnd
                        )
                ).totalAmount(),
                interestAccrualTotalUseCase.execute(
                        new GetCreditInterestAccrualTotalQuery(
                                creditFacilityId, periodStart, periodEnd
                        )
                ).totalInterestAmount(),
                interestAmountUseCase.execute(
                        new GetCreditInterestAmountQuery(
                                creditFacilityId, periodStart, periodEnd
                        )
                ).totalAmount()
        );
    }
}
