package com.example.ledgercore.atm.query.repository;

import com.example.ledgercore.atm.entity.AtmTerminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AtmTerminalQueryRepository
        extends JpaRepository<AtmTerminal, UUID>,
        JpaSpecificationExecutor<AtmTerminal> {
}