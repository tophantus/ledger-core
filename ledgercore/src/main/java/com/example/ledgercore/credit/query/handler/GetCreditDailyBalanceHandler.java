package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.query.dto.CreditDailyBalanceResponse;
import com.example.ledgercore.credit.query.port.inbound.GetCreditDailyBalanceUseCase;
import com.example.ledgercore.credit.query.repository.CreditDailyBalanceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreditDailyBalanceHandler
        implements GetCreditDailyBalanceUseCase {

    private final CreditDailyBalanceQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public CreditDailyBalanceResponse execute(
            UUID creditFacilityId,
            LocalDate businessDate
    ) {
        if (creditFacilityId == null) {
            throw new IllegalArgumentException(
                    "creditFacilityId must not be null"
            );
        }

        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        BigDecimal closingBalance = repository.findEffectiveClosingBalance(
                creditFacilityId,
                businessDate
        );

        return new CreditDailyBalanceResponse(
                creditFacilityId,
                businessDate,
                closingBalance
        );
    }
}
