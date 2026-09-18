package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditFacility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditFacilityQueryRepository
        extends JpaRepository<CreditFacility, UUID> {

    Optional<CreditFacility> findByIdAndCustomerId(
            UUID creditFacilityId,
            UUID customerId
    );
}