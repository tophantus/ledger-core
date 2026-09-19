package com.example.ledgercore.user.query.repository;

import com.example.ledgercore.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserQueryRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u
            FROM User u
            WHERE (:lastProcessedId IS NULL
                   OR u.id > :lastProcessedId)
            ORDER BY u.id ASC
            """)
    List<User> findBatch(
            UUID lastProcessedId,
            Pageable pageable
    );
}
