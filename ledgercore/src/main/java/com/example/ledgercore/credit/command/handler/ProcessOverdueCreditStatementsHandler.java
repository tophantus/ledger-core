package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.service.ProcessOverdueCreditStatementUseCase;
import com.example.ledgercore.credit.command.port.inbound.ProcessOverdueCreditStatementsUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditBusinessDatePort;
import com.example.ledgercore.credit.config.CreditStatementProperties;
import com.example.ledgercore.credit.query.dto.DueCreditStatementResult;
import com.example.ledgercore.credit.query.port.inbound.GetDueCreditStatementsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessOverdueCreditStatementsHandler
        implements ProcessOverdueCreditStatementsUseCase {

    private final GetDueCreditStatementsUseCase
            getDueCreditStatementsUseCase;

    private final ProcessOverdueCreditStatementUseCase
            processOverdueCreditStatementUseCase;

    private final CreditBusinessDatePort
            creditBusinessDatePort;

    private final CreditStatementProperties
            creditStatementProperties;

    @Override
    public void execute() {
        LocalDate businessDate =
                creditBusinessDatePort.getCurrentBusinessDate();

        int batchSize =
                creditStatementProperties
                        .getOverdue()
                        .getBatchSize();

        List<DueCreditStatementResult> statements =
                getDueCreditStatementsUseCase.execute(
                        businessDate,
                        batchSize
                );

        if (statements.isEmpty()) {
            return;
        }

        log.info(
                "Processing {} overdue credit statements " +
                        "for business date {}",
                statements.size(),
                businessDate
        );

        for (DueCreditStatementResult statement : statements) {
            processOverdueCreditStatementUseCase.execute(
                    statement.statementId()
            );
        }

        log.info(
                "Completed processing {} overdue credit statements " +
                        "for business date {}",
                statements.size(),
                businessDate
        );
    }
}