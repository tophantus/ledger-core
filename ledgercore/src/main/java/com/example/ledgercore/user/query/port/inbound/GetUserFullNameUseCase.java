package com.example.ledgercore.user.query.port.inbound;

import java.util.UUID;

public interface GetUserFullNameUseCase {

    String execute(UUID userId);
}