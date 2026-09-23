package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AdminUserAccountDetailResponse;

import java.util.UUID;

public interface GetAdminAccountDetailUseCase {

    AdminUserAccountDetailResponse execute(UUID accountId);
}