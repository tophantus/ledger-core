package com.example.ledgercore.card.command.repository;

import com.example.ledgercore.card.entity.CardAuthorization;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardAuthorizationCommandRepository
        extends JpaRepository<CardAuthorization, UUID> {

    boolean existsByReference(String reference);

    @Query(value = """
        SELECT id
        FROM card_authorizations
        WHERE status = :status
          AND expires_at <= :now
        ORDER BY expires_at ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<UUID> findExpiredAuthorizationIds(
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("limit") int limit
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a
        FROM CardAuthorization a
        WHERE a.id = :authorizationId
        """)
    Optional<CardAuthorization> findByIdForUpdate(
            UUID authorizationId
    );
}