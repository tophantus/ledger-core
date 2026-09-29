package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditRepaymentMandateQueryRepository
        extends JpaRepository<CreditRepaymentMandate, UUID> {

    Optional<CreditRepaymentMandate>
    findByCreditFacilityIdAndStatus(
            UUID creditFacilityId,
            CreditRepaymentMandateStatus status
    );
}