package com.example.ledgercore.user.query.dto;

import java.util.List;
import java.util.UUID;

public record GetUserBatchResult(
        List<UserInfo> users
) {

    public record UserInfo(
            UUID userId
    ) {
    }
}