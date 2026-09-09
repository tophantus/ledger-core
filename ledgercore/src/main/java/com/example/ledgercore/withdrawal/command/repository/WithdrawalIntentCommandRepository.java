package com.example.ledgercore.withdrawal.command.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WithdrawalIntentCommandRepository
        extends JpaRepository<WithdrawalIntent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM WithdrawalIntent w
            WHERE w.id = :intentId
            """)
    Optional<WithdrawalIntent> findByIdForUpdate(
            UUID intentId
    );

    @Query(value = """
            SELECT id
            FROM withdrawal_intents
            WHERE status = :status
              AND expires_at <= :now
            ORDER BY expires_at, id
            LIMIT :limit
            """, nativeQuery = true)
    List<UUID> findExpiredIntentIds(
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("limit") int limit
    );
}