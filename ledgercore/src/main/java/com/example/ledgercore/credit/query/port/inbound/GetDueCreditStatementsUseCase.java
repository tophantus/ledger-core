package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.DueCreditStatementResult;

import java.time.LocalDate;
import java.util.List;

public interface GetDueCreditStatementsUseCase {

    List<DueCreditStatementResult> execute(
            LocalDate businessDate,
            int batchSize
    );
}