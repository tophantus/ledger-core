package com.example.ledgercore.role.query.dto;

import java.util.UUID;

public record GetUserRoleNamesQuery(
        UUID userId
) {
}