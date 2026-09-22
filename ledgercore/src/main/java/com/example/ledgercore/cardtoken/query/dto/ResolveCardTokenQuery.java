package com.example.ledgercore.cardtoken.query.dto;

import java.util.UUID;

public record ResolveCardTokenQuery(
        UUID providerId,
        String token
) {
}