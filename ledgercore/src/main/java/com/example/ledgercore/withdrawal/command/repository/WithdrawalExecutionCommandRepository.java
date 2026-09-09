package com.example.ledgercore.withdrawal.command.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WithdrawalExecutionCommandRepository
        extends JpaRepository<WithdrawalExecution, UUID> {

    boolean existsByWithdrawalIntentId(
            UUID withdrawalIntentId
    );
}