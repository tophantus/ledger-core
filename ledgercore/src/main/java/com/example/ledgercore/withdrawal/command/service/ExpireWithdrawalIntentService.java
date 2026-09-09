package com.example.ledgercore.withdrawal.command.service;

import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpireWithdrawalIntentService {

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final WithdrawalHoldPort withdrawalHoldPort;

    private final Clock clock;

    @Transactional
    public void expire(UUID intentId) {

        Instant now = Instant.now(clock);

        WithdrawalIntent intent =
                withdrawalIntentCommandRepository
                        .findByIdForUpdate(intentId)
                        .orElse(null);

        if (intent == null) {
            return;
        }

        if (!intent.isReady()) {
            return;
        }

        if (!intent.isExpired(now)) {
            return;
        }

        withdrawalHoldPort.releaseHold(
                intent.getHoldId()
        );

        intent.expire();
    }
}