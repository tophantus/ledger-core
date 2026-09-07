package com.example.ledgercore.user.query.dto;

import java.util.UUID;

public record UserAdminInfoResponse(
        UUID id,
        String email,
        String fullName,
        String avatarUrl
) {
}