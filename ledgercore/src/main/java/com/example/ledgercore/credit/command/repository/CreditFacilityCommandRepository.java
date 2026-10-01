package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditFacility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditFacilityCommandRepository
        extends JpaRepository<CreditFacility, UUID> {

    Optional<CreditFacility> findByIdAndCustomerId(
            UUID facilityId,
            UUID customerId
    );

    @Query("""
        SELECT f
        FROM CreditFacility f
        WHERE f.status = com.example.ledgercore.credit.enums.CreditFacilityStatus.ACTIVE
          AND (:lastProcessedId IS NULL OR f.id > :lastProcessedId)
        ORDER BY f.id ASC
        """)
    List<CreditFacility> findBatch(
            @Param("lastProcessedId") UUID lastProcessedId,
            Pageable pageable
    );
}