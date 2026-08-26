package com.example.ledgercore.user.query.port.inbound;

import com.example.ledgercore.user.query.dto.CurrentUserResponse;

import java.util.UUID;

public interface GetCurrentUserUseCase {

    CurrentUserResponse execute(UUID userId);
}