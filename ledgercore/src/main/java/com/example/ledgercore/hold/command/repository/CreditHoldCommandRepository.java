package com.example.ledgercore.hold.command.repository;

import com.example.ledgercore.hold.entity.CreditHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditHoldCommandRepository
        extends JpaRepository<CreditHold, UUID> {
}