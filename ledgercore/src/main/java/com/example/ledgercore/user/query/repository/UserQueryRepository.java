package com.example.ledgercore.user.query.repository;

import com.example.ledgercore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserQueryRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
