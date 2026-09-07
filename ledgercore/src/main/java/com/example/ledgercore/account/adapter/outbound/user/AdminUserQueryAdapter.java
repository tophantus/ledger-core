package com.example.ledgercore.account.adapter.outbound.user;

import com.example.ledgercore.account.query.dto.AdminAccountDetailResponse;
import com.example.ledgercore.account.query.port.outbound.AdminUserQueryPort;
import com.example.ledgercore.user.query.dto.UserAdminInfoResponse;
import com.example.ledgercore.user.query.port.inbound.GetAdminUserInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminUserQueryAdapter
        implements AdminUserQueryPort {

    private final GetAdminUserInfoUseCase
            getAdminUserInfoUseCase;

    @Override
    public AdminAccountDetailResponse.UserInfo getUserAccountInfo(UUID userId) {

        UserAdminInfoResponse response =
                getAdminUserInfoUseCase.execute(userId);

        return new AdminAccountDetailResponse.UserInfo(
                response.id(),
                response.email(),
                response.fullName(),
                response.avatarUrl()
        );
    }
}