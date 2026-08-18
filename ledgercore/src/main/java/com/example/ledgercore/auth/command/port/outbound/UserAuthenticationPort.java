package com.example.ledgercore.auth.command.port.outbound;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserAuthenticationPort {
    Optional<UserAuthenticationInfo> findByEmail(
            String email
    );

    Optional<UserAuthenticationInfo> findById(
            UUID userId
    );

    UUID createUser(
            CreateUserData data
    );

    void activateUser(UUID userId);

    record UserAuthenticationInfo(
            UUID userId,
            String username,
            String email,
            String passwordHash,
            Set<String> roles,
            boolean active
    ) {
    }

    record CreateUserData(
            String username,
            String email,
            String passwordHash
    ) {
    }
}