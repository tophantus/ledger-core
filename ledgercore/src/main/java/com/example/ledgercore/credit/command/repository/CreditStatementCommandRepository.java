package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CreditStatementCommandRepository
        extends JpaRepository<CreditStatement, UUID> {

    boolean existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
            UUID creditFacilityId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    Optional<CreditStatement>
    findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
            UUID creditFacilityId,
            LocalDate periodStart
    );
}
