package com.example.ledgercore.webhook.query.repository;

import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointQueryRepository
        extends JpaRepository<WebhookEndpoint, UUID> {

    Page<WebhookEndpoint> findAllByAccountIdAndStatus(
            UUID accountId,
            WebhookStatus status,
            Pageable pageable
    );

    Page<WebhookEndpoint> findAllByAccountIdInAndStatus(
            List<UUID> accountIds,
            WebhookStatus status,
            Pageable pageable
    );
}