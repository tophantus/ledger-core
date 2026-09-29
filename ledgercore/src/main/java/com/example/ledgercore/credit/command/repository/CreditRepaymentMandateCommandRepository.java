package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditRepaymentMandateCommandRepository
        extends JpaRepository<CreditRepaymentMandate, UUID> {

    boolean existsByCreditFacilityIdAndAccountIdAndStatus(
            UUID creditFacilityId,
            UUID accountId,
            CreditRepaymentMandateStatus status
    );
}