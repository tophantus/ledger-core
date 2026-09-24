package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.mapper.AdminUserAccountMapper;
import com.example.ledgercore.account.query.dto.AdminUserAccountDetailResponse;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetAdminAccountDetailUseCase;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.port.outbound.AdminUserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAdminAccountDetailHandler
        implements GetAdminAccountDetailUseCase {

    private final GetUserAccountService getUserAccountService;
    private final AdminUserQueryPort adminUserQueryPort;

    @Override
    @Transactional(readOnly = true)
    public AdminUserAccountDetailResponse execute(UUID accountId) {

        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        );

        AdminUserAccountDetailResponse.UserInfo user =
                adminUserQueryPort.getUserAccountInfo(
                        account.userId()
                );

        return AdminUserAccountMapper.toDetailResponse(account, user);
    }
}