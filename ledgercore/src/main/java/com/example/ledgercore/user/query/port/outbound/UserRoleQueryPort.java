package com.example.ledgercore.user.query.port.outbound;

import java.util.Set;
import java.util.UUID;

public interface UserRoleQueryPort {

    Set<String> getRoleNames(UUID userId);
}