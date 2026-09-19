package com.example.ledgercore.user.query.dto;

import java.util.UUID;

public record GetUserBatchQuery(
        UUID lastProcessedId,
        int batchSize
) {
}