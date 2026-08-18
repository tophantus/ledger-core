package com.example.ledgercore.outbox.repository;

import com.example.ledgercore.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.*;

import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {
}