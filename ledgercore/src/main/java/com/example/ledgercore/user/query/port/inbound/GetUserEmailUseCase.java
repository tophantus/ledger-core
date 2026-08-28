package com.example.ledgercore.user.query.port.inbound;

import com.example.ledgercore.user.query.dto.UserEmailResponse;

import java.util.UUID;

public interface GetUserEmailUseCase {

    UserEmailResponse execute(UUID userId);
}

