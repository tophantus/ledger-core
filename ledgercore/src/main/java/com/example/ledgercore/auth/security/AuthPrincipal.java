package com.example.ledgercore.auth.security;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthPrincipal {
    private final UUID userId;
}