package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.withdrawal.command.port.inbound.DeleteExpiredWithdrawalLookupCodesUseCase;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalLookupCodeCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteExpiredWithdrawalLookupCodesHandler
        implements DeleteExpiredWithdrawalLookupCodesUseCase {

    private final WithdrawalLookupCodeCommandRepository
            withdrawalLookupCodeCommandRepository;

    private final Clock clock;

    @Override
    @Transactional
    public void execute() {
        Instant now = Instant.now(clock);

        int deletedCount =
                withdrawalLookupCodeCommandRepository
                        .deleteExpired(now);

        if (deletedCount == 0) {
            return;
        }

        log.info(
                "Deleted expired withdrawal lookup codes: count={}",
                deletedCount
        );
    }
}