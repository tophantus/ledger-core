package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditStatement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CreditStatementCommandRepository
        extends JpaRepository<CreditStatement, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM CreditStatement s
        WHERE s.id = :statementId
        """)
    Optional<CreditStatement> findByIdForUpdate(
            @Param("statementId") UUID statementId
    );

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
