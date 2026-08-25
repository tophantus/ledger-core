package com.example.ledgercore.webhook.query.repository;

import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryQueryRepository
        extends JpaRepository<WebhookDelivery, UUID> {

    @Query("""
            select d
            from WebhookDelivery d
            where
                d.status = :pending
                or (
                    d.status = :retrying
                    and d.nextAttemptAt <= :now
                )
            order by d.createdAt asc
            """)
    List<WebhookDelivery> findDueDeliveries(
            @Param("pending") WebhookDeliveryStatus pending,
            @Param("retrying") WebhookDeliveryStatus retrying,
            @Param("now") Instant now,
            Pageable pageable
    );

    List<WebhookDelivery>
    findByStatusAndAttemptStartedAtBefore(
            WebhookDeliveryStatus status,
            Instant threshold,
            Pageable pageable
    );
}