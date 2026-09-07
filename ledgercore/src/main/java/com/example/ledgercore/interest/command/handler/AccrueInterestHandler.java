package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.AccrueInterestCommand;
import com.example.ledgercore.interest.command.port.inbound.AccrueInterestUseCase;
import com.example.ledgercore.interest.command.port.outbound.AccountDailyBalanceInfo;
import com.example.ledgercore.interest.command.port.outbound.AccountDailyBalancePort;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.interest.command.repository.InterestAccrualCommandRepository;
import com.example.ledgercore.interest.entity.InterestAccrual;
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
public class AccrueInterestHandler
        implements AccrueInterestUseCase {

    private final AccountDailyBalancePort accountDailyBalancePort;
    private final InterestConfigService interestConfigService;
    private final InterestCalculationService interestCalculationService;
    private final InterestAccrualCommandRepository interestAccrualCommandRepository;
    private final InterestJournalPort interestJournalPort;

    @Override
    @Transactional
    public void execute(
            AccrueInterestCommand command
    ) {
        validateCommand(command);

        InterestAccrual existing =
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDate(
                                command.accountId(),
                                command.businessDate()
                        )
                        .orElse(null);

        if (existing != null) {
            return;
        }

        AccountDailyBalanceInfo dailyBalance =
                accountDailyBalancePort.findClosingBalance(
                        command.accountId(),
                        command.businessDate()
                );

        BigDecimal principal =
                dailyBalance.closingBalance();

        InterestConfig config =
                interestConfigService.getApplicableConfig(
                        command.productId(),
                        command.currency(),
                        command.businessDate()
                );

        BigDecimal interestAmount =
                interestCalculationService.calculateDailyInterest(
                        principal,
                        config.getInterestRate(),
                        config.getDayCountConvention()
                );

        InterestAccrual accrual =
                InterestAccrual.builder()
                        .runId(command.runId())
                        .accountId(command.accountId())
                        .currency(command.currency())
                        .businessDate(command.businessDate())
                        .interestConfigId(config.getId())
                        .principalAmount(principal)
                        .interestRate(config.getInterestRate())
                        .interestAmount(interestAmount)
                        .build();

        accrual =
                interestAccrualCommandRepository.save(accrual);

        if (interestAmount.signum() == 0) {
            return;
        }

        UUID journalEntryId =
                interestJournalPort.recordAccrualJournal(
                        accrual.getId(),
                        command.businessDate(),
                        command.currency(),
                        interestAmount
                );

        accrual.setJournalEntryId(journalEntryId);

        interestAccrualCommandRepository.save(accrual);
    }

    private void validateCommand(
            AccrueInterestCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "command must not be null"
            );
        }

        if (command.runId() == null) {
            throw new IllegalArgumentException(
                    "runId must not be null"
            );
        }

        if (command.accountId() == null) {
            throw new IllegalArgumentException(
                    "accountId must not be null"
            );
        }

        if (command.productId() == null) {

            throw new IllegalArgumentException(
                    "productCode must not be null"
            );
        }

        if (command.currency() == null
                || command.currency().isBlank()) {

            throw new IllegalArgumentException(
                    "currency must not be blank"
            );
        }

        if (command.businessDate() == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }
    }
}