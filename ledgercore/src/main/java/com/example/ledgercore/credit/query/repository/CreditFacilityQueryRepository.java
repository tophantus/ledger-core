package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditFacilityQueryRepository
        extends JpaRepository<CreditFacility, UUID> {

    Optional<CreditFacility> findByIdAndCustomerId(
            UUID creditFacilityId,
            UUID customerId
    );

    Optional<CreditFacility> findFirstByCustomerIdAndStatus(
            UUID customerId,
            CreditFacilityStatus status
    );

    @Query("""
        SELECT f
        FROM CreditFacility f
        WHERE f.status = :status
          AND (:lastProcessedId IS NULL OR f.id > :lastProcessedId)
        ORDER BY f.id ASC
        """)
    List<CreditFacility> findBatch(
            @Param("lastProcessedId") UUID lastProcessedId,
            @Param("status") CreditFacilityStatus status,
            Pageable pageable
    );
}