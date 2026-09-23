package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.credit.command.repository.CreditDailyBalanceCommandRepository;
import com.example.ledgercore.credit.command.service.CreditDailyBalanceService;
import com.example.ledgercore.credit.entity.CreditDailyBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditDailyBalanceServiceImpl
        implements CreditDailyBalanceService {

    private final CreditDailyBalanceCommandRepository repository;

    @Override
    public void updateClosingBalance(
            UUID creditFacilityId,
            LocalDate businessDate,
            BigDecimal closingBalance
    ) {
        CreditDailyBalance dailyBalance =
                repository
                        .findByCreditFacilityIdAndBusinessDate(
                                creditFacilityId,
                                businessDate
                        )
                        .orElseGet(() ->
                                CreditDailyBalance.builder()
                                        .id(UUID.randomUUID())
                                        .creditFacilityId(creditFacilityId)
                                        .businessDate(businessDate)
                                        .closingBalance(closingBalance)
                                        .createdAt(Instant.now())
                                        .build()
                        );

        dailyBalance.updateClosingBalance(closingBalance);

        repository.save(dailyBalance);
    }
}