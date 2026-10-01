package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.GetDueRepaymentCandidatesResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface GetDueRepaymentCandidatesUseCase {

    List<GetDueRepaymentCandidatesResult> execute(
            Instant now,
            int batchSize
    );
}