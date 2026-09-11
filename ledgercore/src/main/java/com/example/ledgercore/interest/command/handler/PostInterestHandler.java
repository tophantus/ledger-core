package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.dto.PostInterestCommand;
import com.example.ledgercore.interest.command.port.inbound.PostInterestUseCase;
import com.example.ledgercore.interest.command.port.outbound.InterestTransactionPort;
import com.example.ledgercore.interest.command.repository.InterestAccrualCommandRepository;
import com.example.ledgercore.interest.command.repository.InterestPostingCommandRepository;
import com.example.ledgercore.interest.entity.InterestAccrual;
import com.example.ledgercore.interest.entity.InterestPosting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostInterestHandler
        implements PostInterestUseCase {

    private final InterestAccrualCommandRepository
            interestAccrualCommandRepository;

    private final InterestPostingCommandRepository
            interestPostingCommandRepository;

    private final InterestTransactionPort
            interestTransactionPort;

    @Override
    @Transactional
    public void execute(
            PostInterestCommand command
    ) {
        validateCommand(command);

        boolean alreadyPosted =
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                command.accountId(),
                                command.periodStart(),
                                command.periodEnd()
                        );

        if (alreadyPosted) {
            return;
        }

        List<InterestAccrual> accruals =
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                                command.accountId(),
                                command.periodStart(),
                                command.periodEnd()
                        );

        if (accruals.isEmpty()) {
            return;
        }

        validateCurrencies(accruals);

        BigDecimal totalInterest =
                accruals.stream()
                        .map(InterestAccrual::getInterestAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        Currency currency =
                accruals.getFirst().getCurrency();

        InterestPosting posting =
                InterestPosting.builder()
                        .runId(command.runId())
                        .accountId(command.accountId())
                        .periodStart(command.periodStart())
                        .periodEnd(command.periodEnd())
                        .interestAmount(totalInterest)
                        .build();

        posting =
                interestPostingCommandRepository.save(
                        posting
                );

        if (totalInterest.signum() == 0) {
            markAccrualsAsPosted(
                    accruals,
                    posting.getId()
            );

            return;
        }

        UUID transactionId =
                interestTransactionPort.postInterest(
                        command.accountId(),
                        totalInterest,
                        currency,
                        command.periodEnd()
                );

        posting.setTransactionId(transactionId);
        posting.setPostedAt(Instant.now());

        interestPostingCommandRepository.save(posting);

        markAccrualsAsPosted(
                accruals,
                posting.getId()
        );
    }

    private void markAccrualsAsPosted(
            List<InterestAccrual> accruals,
            UUID postingId
    ) {
        accruals.forEach(accrual ->
                accrual.setPostingId(postingId)
        );

        interestAccrualCommandRepository.saveAll(
                accruals
        );
    }

    private void validateCurrencies(
            List<InterestAccrual> accruals
    ) {
        Currency currency =
                accruals.getFirst().getCurrency();

        boolean sameCurrency =
                accruals.stream()
                        .allMatch(accrual ->
                                currency.equals(
                                        accrual.getCurrency()
                                )
                        );

        if (!sameCurrency) {
            throw new IllegalArgumentException(
                    "Interest accruals must have the same currency"
            );
        }
    }

    private void validateCommand(
            PostInterestCommand command
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

        LocalDate periodStart =
                command.periodStart();

        LocalDate periodEnd =
                command.periodEnd();

        if (periodStart == null) {
            throw new IllegalArgumentException(
                    "periodStart must not be null"
            );
        }

        if (periodEnd == null) {
            throw new IllegalArgumentException(
                    "periodEnd must not be null"
            );
        }

        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException(
                    "periodStart must not be after periodEnd"
            );
        }
    }
}
