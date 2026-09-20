package com.example.ledgercore.credit.command.repository;

import com.example.ledgercore.credit.entity.CreditOfferRun;
import com.example.ledgercore.credit.enums.CreditOfferRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CreditOfferRunCommandRepository
        extends JpaRepository<CreditOfferRun, UUID> {

    @Query(
            value = """
            SELECT *
            FROM credit_offer_runs
            WHERE
                status = :pendingStatus
                OR (
                    status = :runningStatus
                    AND heartbeat_at < :staleBefore
                )
            ORDER BY business_date ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true
    )
    Optional<CreditOfferRun> findClaimableRun(
            @Param("pendingStatus") String pendingStatus,
            @Param("runningStatus") String runningStatus,
            @Param("staleBefore") Instant staleBefore
    );

}
