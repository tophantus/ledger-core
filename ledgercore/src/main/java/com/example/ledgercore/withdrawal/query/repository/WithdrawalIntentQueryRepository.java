package com.example.ledgercore.withdrawal.query.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface WithdrawalIntentQueryRepository
        extends JpaRepository<WithdrawalIntent, UUID>,
        JpaSpecificationExecutor<WithdrawalIntent> {
}