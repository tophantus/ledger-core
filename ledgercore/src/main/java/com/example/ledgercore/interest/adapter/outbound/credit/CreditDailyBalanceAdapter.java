package com.example.ledgercore.interest.adapter.outbound.credit;

import com.example.ledgercore.credit.query.port.inbound.GetCreditDailyBalanceUseCase;
import com.example.ledgercore.interest.command.port.outbound.credit.CreditDailyBalanceInfo;
import com.example.ledgercore.interest.command.port.outbound.credit.CreditDailyBalancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditDailyBalanceAdapter
        implements CreditDailyBalancePort {

    private final GetCreditDailyBalanceUseCase useCase;

    @Override
    public CreditDailyBalanceInfo findClosingBalance(
            UUID creditFacilityId,
            LocalDate businessDate
    ) {
        var response = useCase.execute(
                creditFacilityId,
                businessDate
        );

        return new CreditDailyBalanceInfo(
                response.creditFacilityId(),
                response.businessDate(),
                response.closingBalance()
        );
    }
}
