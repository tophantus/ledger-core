package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.port.inbound.CreateInterestAccrualRunUseCase;
import com.example.ledgercore.interest.command.repository.InterestRunCommandRepository;
import com.example.ledgercore.interest.entity.InterestRun;
import com.example.ledgercore.interest.enums.InterestRunStatus;
import com.example.ledgercore.interest.enums.InterestRunType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateInterestAccrualRunHandler
        implements CreateInterestAccrualRunUseCase {

    private final InterestRunCommandRepository
            interestRunCommandRepository;

    @Override
    @Transactional
    public void execute(LocalDate businessDate) {

        if (interestRunCommandRepository
                .existsByBusinessDateAndRunType(
                        businessDate,
                        InterestRunType.ACCRUAL
                )) {
            return;
        }

        InterestRun interestRun =
                InterestRun.builder()
                        .businessDate(businessDate)
                        .runType(InterestRunType.ACCRUAL)
                        .status(InterestRunStatus.PENDING)
                        .processedCount(0L)
                        .createdAt(Instant.now())
                        .build();

        interestRunCommandRepository.save(interestRun);
    }
}