package com.example.ledgercore.account.adapter.outbound.user;

import com.example.ledgercore.account.query.dto.AdminUserAccountDetailResponse;
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
    public AdminUserAccountDetailResponse.UserInfo getUserAccountInfo(UUID userId) {

        UserAdminInfoResponse response =
                getAdminUserInfoUseCase.execute(userId);

        return new AdminUserAccountDetailResponse.UserInfo(
                response.id(),
                response.email(),
                response.fullName(),
                response.avatarUrl()
        );
    }
}