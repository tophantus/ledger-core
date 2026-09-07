package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AdminAccountDetailResponse;

import java.util.UUID;

public interface GetAdminAccountDetailUseCase {

    AdminAccountDetailResponse execute(UUID accountId);
}