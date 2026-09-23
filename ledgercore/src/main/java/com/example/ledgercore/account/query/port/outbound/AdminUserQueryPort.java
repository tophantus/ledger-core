package com.example.ledgercore.account.query.port.outbound;

import com.example.ledgercore.account.query.dto.AdminUserAccountDetailResponse;

import java.util.UUID;

public interface AdminUserQueryPort {

    AdminUserAccountDetailResponse.UserInfo getUserAccountInfo(UUID userId);
}