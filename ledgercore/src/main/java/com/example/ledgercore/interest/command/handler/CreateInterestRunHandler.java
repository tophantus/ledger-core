package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.port.inbound.CreateInterestRunUseCase;
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
public class CreateInterestRunHandler
        implements CreateInterestRunUseCase {

    private final InterestRunCommandRepository
            interestRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            LocalDate businessDate,
            InterestRunType runType
    ) {
        validate(
                businessDate,
                runType
        );

        if (interestRunCommandRepository
                .existsByBusinessDateAndRunType(
                        businessDate,
                        runType
                )) {
            return;
        }

        InterestRun interestRun =
                InterestRun.builder()
                        .businessDate(businessDate)
                        .runType(runType)
                        .status(InterestRunStatus.PENDING)
                        .processedCount(0L)
                        .createdAt(Instant.now())
                        .build();

        interestRunCommandRepository.save(
                interestRun
        );
    }

    private void validate(
            LocalDate businessDate,
            InterestRunType runType
    ) {
        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        if (runType == null) {
            throw new IllegalArgumentException(
                    "runType must not be null"
            );
        }
    }
}