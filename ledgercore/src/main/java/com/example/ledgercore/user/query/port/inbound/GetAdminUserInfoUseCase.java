package com.example.ledgercore.user.query.port.inbound;

import com.example.ledgercore.user.query.dto.UserAdminInfoResponse;

import java.util.UUID;

public interface GetAdminUserInfoUseCase {

    UserAdminInfoResponse execute(UUID userId);
}