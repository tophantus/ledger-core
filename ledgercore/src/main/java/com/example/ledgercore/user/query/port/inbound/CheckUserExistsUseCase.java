package com.example.ledgercore.user.query.port.inbound;

import java.util.UUID;

public interface CheckUserExistsUseCase {

    boolean execute(UUID userId);
}