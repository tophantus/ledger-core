package com.example.ledgercore.outbox.command.repository;

import com.example.ledgercore.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventCommandRepository extends JpaRepository<OutboxEvent, UUID> {
}
