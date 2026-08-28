package com.example.ledgercore.transaction.query.repository;

import com.example.ledgercore.transaction.entity.TransferIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferIntentQueryRepository extends JpaRepository<TransferIntent, UUID> {
}
