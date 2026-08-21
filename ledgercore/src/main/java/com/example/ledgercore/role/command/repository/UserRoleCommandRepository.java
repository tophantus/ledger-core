package com.example.ledgercore.role.command.repository;

import com.example.ledgercore.role.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleCommandRepository
        extends JpaRepository<UserRole, UUID> {

    boolean existsByUserIdAndRoleId(
            UUID userId,
            UUID roleId
    );
}