package com.example.ledgercore.account.query.port.outbound;

import com.example.ledgercore.account.query.dto.AdminAccountDetailResponse;

import java.util.UUID;

public interface AdminUserQueryPort {

    AdminAccountDetailResponse.UserInfo getUserAccountInfo(UUID userId);
}