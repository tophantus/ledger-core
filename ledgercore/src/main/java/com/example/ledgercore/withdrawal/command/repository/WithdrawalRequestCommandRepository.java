package com.example.ledgercore.withdrawal.command.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WithdrawalRequestCommandRepository
        extends JpaRepository<WithdrawalRequest, UUID> {
}