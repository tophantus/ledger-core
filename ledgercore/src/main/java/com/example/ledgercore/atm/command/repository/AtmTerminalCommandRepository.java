package com.example.ledgercore.atm.command.repository;

import com.example.ledgercore.atm.entity.AtmTerminal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AtmTerminalCommandRepository
        extends JpaRepository<AtmTerminal, UUID> {

    boolean existsByTerminalCode(String terminalCode);
}