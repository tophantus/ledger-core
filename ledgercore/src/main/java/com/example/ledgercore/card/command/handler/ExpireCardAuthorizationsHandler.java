package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.port.inbound.ExpireCardAuthorizationsUseCase;
import com.example.ledgercore.card.command.service.ExpireCardAuthorizationService;
import com.example.ledgercore.card.command.service.GetExpiredCardAuthorizationIdsService;
import com.example.ledgercore.card.config.CardAuthorizationExpirationProperties;
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
public class ExpireCardAuthorizationsHandler
        implements ExpireCardAuthorizationsUseCase {

    private final GetExpiredCardAuthorizationIdsService
            getExpiredCardAuthorizationIdsService;

    private final ExpireCardAuthorizationService
            expireCardAuthorizationService;

    private final CardAuthorizationExpirationProperties
            cardAuthorizationExpirationProperties;

    private final Clock clock;

    @Override
    public void execute() {

        Instant now = Instant.now(clock);

        List<UUID> authorizationIds =
                getExpiredCardAuthorizationIdsService.get(
                        now,
                        cardAuthorizationExpirationProperties.getBatchSize()
                );

        if (authorizationIds.isEmpty()) {
            return;
        }

        log.info(
                "Processing expired card authorizations: count={}",
                authorizationIds.size()
        );

        int successCount = 0;
        int failedCount = 0;

        for (UUID authorizationId : authorizationIds) {
            try {
                expireCardAuthorizationService.expire(
                        authorizationId
                );

                successCount++;
            } catch (Exception e) {
                failedCount++;

                log.error(
                        "Failed to expire card authorization: authorizationId={}",
                        authorizationId,
                        e
                );
            }
        }

        log.info(
                "Finished processing expired card authorizations: total={}, success={}, failed={}",
                authorizationIds.size(),
                successCount,
                failedCount
        );
    }
}