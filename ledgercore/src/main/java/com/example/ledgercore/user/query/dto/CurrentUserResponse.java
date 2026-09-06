package com.example.ledgercore.user.query.dto;

import com.example.ledgercore.user.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        UserStatus status,
        Set<String> roles,
        UserProfileResponse profile,
        Instant createdAt
) {
}