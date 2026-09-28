package com.example.ledgercore.interest.command.service.impl;

import com.example.ledgercore.interest.command.dto.AccrueCreditInterestCommand;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.interest.command.port.outbound.credit.CreditDailyBalanceInfo;
import com.example.ledgercore.interest.command.port.outbound.credit.CreditDailyBalancePort;
import com.example.ledgercore.interest.command.repository.CreditInterestAccrualCommandRepository;
import com.example.ledgercore.interest.command.service.AccrueCreditInterestUseCase;
import com.example.ledgercore.interest.entity.CreditInterestAccrual;
import com.example.ledgercore.interest.entity.InterestConfig;
import com.example.ledgercore.interest.service.InterestCalculationService;
import com.example.ledgercore.interest.service.InterestConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccrueCreditInterestHandler
        implements AccrueCreditInterestUseCase {

    private final CreditDailyBalancePort creditDailyBalancePort;
    private final InterestConfigService interestConfigService;
    private final InterestCalculationService interestCalculationService;
    private final CreditInterestAccrualCommandRepository repository;
    private final InterestJournalPort interestJournalPort;

    @Override
    @Transactional
    public void execute(AccrueCreditInterestCommand command) {
        validateCommand(command);

        if (repository.findByCreditFacilityIdAndBusinessDate(
                command.creditFacilityId(),
                command.businessDate()
        ).isPresent()) {
            return;
        }

        CreditDailyBalanceInfo dailyBalance =
                creditDailyBalancePort.findClosingBalance(
                        command.creditFacilityId(),
                        command.businessDate()
                );

        InterestConfig config = interestConfigService.getApplicableConfig(
                command.productId(),
                command.currency(),
                command.businessDate()
        );

        BigDecimal interestAmount =
                interestCalculationService.calculateDailyInterest(
                        dailyBalance.closingBalance(),
                        config.getInterestRate(),
                        config.getDayCountConvention()
                );

        CreditInterestAccrual accrual = repository.save(
                CreditInterestAccrual.builder()
                        .runId(command.runId())
                        .creditFacilityId(command.creditFacilityId())
                        .businessDate(command.businessDate())
                        .principalAmount(dailyBalance.closingBalance())
                        .interestRate(config.getInterestRate())
                        .interestAmount(interestAmount)
                        .build()
        );

        if (interestAmount.signum() == 0) {
            return;
        }

        UUID journalEntryId = interestJournalPort.recordAccrualJournal(
                accrual.getId(),
                command.businessDate(),
                command.currency(),
                interestAmount
        );

        accrual.setJournalEntryId(journalEntryId);
        repository.save(accrual);
    }

    private void validateCommand(AccrueCreditInterestCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        if (command.runId() == null) {
            throw new IllegalArgumentException("runId must not be null");
        }

        if (command.creditFacilityId() == null) {
            throw new IllegalArgumentException(
                    "creditFacilityId must not be null"
            );
        }

        if (command.productId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }

        if (command.currency() == null) {
            throw new IllegalArgumentException("currency must not be null");
        }

        if (command.businessDate() == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }
    }
}
