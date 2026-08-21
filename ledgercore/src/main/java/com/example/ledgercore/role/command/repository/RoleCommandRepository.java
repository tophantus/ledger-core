package com.example.ledgercore.role.command.repository;

import com.example.ledgercore.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleCommandRepository
        extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);
}