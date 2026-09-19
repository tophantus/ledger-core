package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditOfferRun;
import com.example.ledgercore.credit.enums.CreditOfferRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CreditOfferRunCommandRepository
        extends JpaRepository<CreditOfferRun, UUID> {

    @Query("""
        SELECT r
        FROM CreditOfferRun r
        WHERE r.status = :pendingStatus
           OR (
                r.status = :runningStatus
                AND r.heartbeatAt < :staleBefore
           )
        ORDER BY r.businessDate ASC
        """)
    Optional<CreditOfferRun> findFirstClaimable(
            CreditOfferRunStatus pendingStatus,
            CreditOfferRunStatus runningStatus,
            Instant staleBefore
    );

}
