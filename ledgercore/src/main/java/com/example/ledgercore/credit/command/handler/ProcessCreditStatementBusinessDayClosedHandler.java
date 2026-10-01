package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.ProcessCreditStatementBusinessDayClosedUseCase;
import com.example.ledgercore.credit.command.repository.CreditStatementRunCommandRepository;
import com.example.ledgercore.credit.entity.CreditStatementRun;
import com.example.ledgercore.credit.enums.CreditStatementRunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProcessCreditStatementBusinessDayClosedHandler
        implements ProcessCreditStatementBusinessDayClosedUseCase {

    private final CreditStatementRunCommandRepository
            creditStatementRunCommandRepository;

    @Override
    @Transactional
    public void execute(LocalDate businessDate) {

        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        if (businessDate.getDayOfMonth()
                != businessDate.lengthOfMonth()) {
            return;
        }

        LocalDate periodStart =
                businessDate.withDayOfMonth(1);

        LocalDate periodEnd =
                businessDate;

        LocalDate statementDate =
                businessDate;

        LocalDate dueDate =
                statementDate.plusDays(20);

        if (creditStatementRunCommandRepository
                .existsByPeriodStartAndPeriodEnd(
                        periodStart,
                        periodEnd
                )) {
            return;
        }

        CreditStatementRun run =
                CreditStatementRun.builder()
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .statementDate(statementDate)
                        .dueDate(dueDate)
                        .status(CreditStatementRunStatus.PENDING)
                        .build();

        creditStatementRunCommandRepository.save(run);
    }
}