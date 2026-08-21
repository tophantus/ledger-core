package com.example.ledgercore.role.command.dto;

import java.util.UUID;

public record AssignCustomerRoleCommand(
        UUID userId
) {
}