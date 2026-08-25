package com.example.ledgercore.webhook.query.repository;

import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointQueryRepository
        extends JpaRepository<WebhookEndpoint, UUID> {

    List<WebhookEndpoint> findAllByAccountId(UUID accountId);
}