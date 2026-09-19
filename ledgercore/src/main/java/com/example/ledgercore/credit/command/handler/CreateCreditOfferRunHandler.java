package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.CreateCreditOfferRunUseCase;
import com.example.ledgercore.credit.command.repository.CreditOfferRunCommandRepository;
import com.example.ledgercore.credit.entity.CreditOfferRun;
import com.example.ledgercore.credit.enums.CreditOfferRunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateCreditOfferRunHandler
        implements CreateCreditOfferRunUseCase {

    private final CreditOfferRunCommandRepository creditOfferRunCommandRepository;

    @Override
    @Transactional
    public void execute(LocalDate businessDate) {
        CreditOfferRun run = CreditOfferRun.builder()
                .businessDate(businessDate)
                .status(CreditOfferRunStatus.PENDING)
                .build();

        creditOfferRunCommandRepository.save(run);
    }
}