package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditOffer;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditOfferQueryRepository
        extends JpaRepository<CreditOffer, UUID> {

    @Query("""
            SELECT o.customerId
            FROM CreditOffer o
            WHERE o.customerId IN :customerIds
              AND o.status = :status
              AND o.expiresAt > :now
            """)
    List<UUID> findCustomerIdsWithActiveOffer(
            @Param("customerIds") Collection<UUID> customerIds,
            @Param("status") CreditOfferStatus status,
            @Param("now") Instant now
    );

    Optional<CreditOffer> findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            UUID customerId,
            CreditOfferStatus status,
            Instant now
    );
}