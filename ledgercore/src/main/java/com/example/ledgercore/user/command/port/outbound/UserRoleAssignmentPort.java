package com.example.ledgercore.user.command.port.outbound;

import java.util.UUID;

public interface UserRoleAssignmentPort {

    void assignCustomerRole(UUID userId);
}