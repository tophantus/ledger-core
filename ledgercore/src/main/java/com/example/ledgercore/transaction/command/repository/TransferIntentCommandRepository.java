package com.example.ledgercore.transaction.command.repository;

import com.example.ledgercore.transaction.entity.TransferIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferIntentCommandRepository extends JpaRepository<TransferIntent, UUID> {
}
