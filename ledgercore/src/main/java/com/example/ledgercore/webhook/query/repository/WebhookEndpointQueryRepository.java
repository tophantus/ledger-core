package com.example.ledgercore.webhook.query.repository;

import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointQueryRepository
        extends JpaRepository<WebhookEndpoint, UUID> {

    Page<WebhookEndpoint> findAllByAccountId(
            UUID accountId,
            Pageable pageable
    );

    Page<WebhookEndpoint> findAllByAccountIdIn(
            List<UUID> accountIds,
            Pageable pageable
    );
}