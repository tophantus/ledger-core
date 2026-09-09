package com.example.ledgercore.withdrawal.command.service;

import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetExpiredWithdrawalIntentIdsService {

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    public List<UUID> get(
            Instant now,
            int limit
    ) {
        return withdrawalIntentCommandRepository
                .findExpiredIntentIds(
                        WithdrawalIntentStatus.READY.name(),
                        now,
                        limit
                );
    }
}