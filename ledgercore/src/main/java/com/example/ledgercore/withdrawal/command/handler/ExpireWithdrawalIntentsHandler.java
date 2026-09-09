package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.withdrawal.command.port.inbound.ExpireWithdrawalIntentsUseCase;
import com.example.ledgercore.withdrawal.command.service.ExpireWithdrawalIntentService;
import com.example.ledgercore.withdrawal.command.service.GetExpiredWithdrawalIntentIdsService;
import com.example.ledgercore.withdrawal.config.WithdrawalExpirationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpireWithdrawalIntentsHandler
        implements ExpireWithdrawalIntentsUseCase {

    private final GetExpiredWithdrawalIntentIdsService
            getExpiredWithdrawalIntentIdsService;

    private final ExpireWithdrawalIntentService
            expireWithdrawalIntentService;

    private final WithdrawalExpirationProperties
            withdrawalExpirationProperties;

    private final Clock clock;

    @Override
    public void execute() {

        Instant now = Instant.now(clock);

        List<UUID> intentIds =
                getExpiredWithdrawalIntentIdsService.get(
                        now,
                        withdrawalExpirationProperties.getBatchSize()
                );

        if (intentIds.isEmpty()) {
            return;
        }

        log.info(
                "Processing expired withdrawal intents: count={}",
                intentIds.size()
        );

        int successCount = 0;
        int failedCount = 0;

        for (UUID intentId : intentIds) {
            try {
                expireWithdrawalIntentService.expire(intentId);
                successCount++;
            } catch (Exception e) {
                failedCount++;

                log.error(
                        "Failed to expire withdrawal intent: intentId={}",
                        intentId,
                        e
                );
            }
        }

        log.info(
                "Finished processing expired withdrawal intents: total={}, success={}, failed={}",
                intentIds.size(),
                successCount,
                failedCount
        );
    }
}