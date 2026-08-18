package com.example.ledgercore.user.command.repository;

import com.example.ledgercore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCommandRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
}