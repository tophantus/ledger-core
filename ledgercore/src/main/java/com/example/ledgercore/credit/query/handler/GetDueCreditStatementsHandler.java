package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.enums.CreditStatementStatus;
import com.example.ledgercore.credit.query.dto.DueCreditStatementResult;
import com.example.ledgercore.credit.query.port.inbound.GetDueCreditStatementsUseCase;
import com.example.ledgercore.credit.query.repository.CreditStatementQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDueCreditStatementsHandler
        implements GetDueCreditStatementsUseCase {

    private final CreditStatementQueryRepository
            creditStatementQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DueCreditStatementResult> execute(
            LocalDate businessDate,
            int batchSize
    ) {
        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }

        List<CreditStatementStatus> statuses =
                List.of(
                        CreditStatementStatus.ISSUED,
                        CreditStatementStatus.PARTIALLY_PAID
                );

        return creditStatementQueryRepository
                .findDueStatementIds(
                        statuses,
                        businessDate,
                        PageRequest.of(
                                0,
                                batchSize
                        )
                )
                .stream()
                .map(DueCreditStatementResult::new)
                .toList();
    }
}