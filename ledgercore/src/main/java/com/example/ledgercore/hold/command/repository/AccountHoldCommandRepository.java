package com.example.ledgercore.hold.command.repository;

import com.example.ledgercore.hold.entity.AccountHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountHoldCommandRepository
        extends JpaRepository<AccountHold, UUID> {
}