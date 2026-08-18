package com.example.ledgercore.user.query.port.inbound;

import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;

import java.util.Optional;
import java.util.UUID;

public interface GetUserAuthenticationUseCase {

    Optional<UserAuthenticationResponse> findByEmail(
            String email
    );

    Optional<UserAuthenticationResponse> findById(
            UUID userId
    );
}