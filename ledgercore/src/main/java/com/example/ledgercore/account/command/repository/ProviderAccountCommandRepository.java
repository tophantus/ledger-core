package com.example.ledgercore.account.command.repository;

import com.example.ledgercore.account.entity.ProviderAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProviderAccountCommandRepository
        extends JpaRepository<ProviderAccount, UUID> {

    boolean existsByAccountId(UUID accountId);
}