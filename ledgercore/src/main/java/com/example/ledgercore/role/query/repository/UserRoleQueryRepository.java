package com.example.ledgercore.role.query.repository;

import com.example.ledgercore.role.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.UUID;

public interface UserRoleQueryRepository
        extends JpaRepository<UserRole, UUID> {

    @Query("""
            SELECT ur.role.name
            FROM UserRole ur
            WHERE ur.userId = :userId
            """)
    Set<String> findRoleNamesByUserId(UUID userId);
}