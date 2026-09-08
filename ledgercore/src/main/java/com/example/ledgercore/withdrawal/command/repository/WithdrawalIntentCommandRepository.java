package com.example.ledgercore.withdrawal.command.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WithdrawalIntentCommandRepository
        extends JpaRepository<WithdrawalIntent, UUID> {
}