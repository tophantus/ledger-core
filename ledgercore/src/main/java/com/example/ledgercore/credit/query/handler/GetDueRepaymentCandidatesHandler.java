package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import com.example.ledgercore.credit.enums.RepaymentType;
import com.example.ledgercore.credit.query.dto.GetDueRepaymentCandidatesResult;
import com.example.ledgercore.credit.query.port.inbound.GetDueRepaymentCandidatesUseCase;
import com.example.ledgercore.credit.query.repository.CreditStatementQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDueRepaymentCandidatesHandler
        implements GetDueRepaymentCandidatesUseCase {

    private static final List<CreditStatementStatus> ELIGIBLE_STATUSES = List.of(
            CreditStatementStatus.ISSUED,
            CreditStatementStatus.PARTIALLY_PAID
    );

    private final CreditStatementQueryRepository creditStatementQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GetDueRepaymentCandidatesResult> execute(
            Instant now,
            int batchSize
    ) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than zero");
        }

        return creditStatementQueryRepository.findDueRepaymentCandidates(
                CreditRepaymentMandateStatus.ACTIVE,
                ELIGIBLE_STATUSES,
                RepaymentType.FULL_BALANCE,
                RepaymentType.MINIMUM_PAYMENT,
                now,
                PageRequest.of(0, batchSize)
        );
    }
}