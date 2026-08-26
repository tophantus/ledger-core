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

    Optional<UserAuthenticationInfo> createUser(
            CreateUserData data
    );

    void activateUser(UUID userId);

    void updatePassword(
            UUID userId,
            String passwordHash
    );

    record UserAuthenticationInfo(
            UUID userId,
            String email,
            String passwordHash,
            Set<String> roles,
            boolean active
    ) {
    }

    record CreateUserData(
            String displayName,
            String email,
            String passwordHash
    ) {
    }
}