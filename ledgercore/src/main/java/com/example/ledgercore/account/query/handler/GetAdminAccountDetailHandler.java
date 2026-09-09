package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.mapper.AdminAccountMapper;
import com.example.ledgercore.account.query.dto.AdminAccountDetailResponse;
import com.example.ledgercore.account.query.port.inbound.GetAdminAccountDetailUseCase;
import com.example.ledgercore.account.query.port.outbound.AdminUserQueryPort;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAdminAccountDetailHandler
        implements GetAdminAccountDetailUseCase {

    private final AccountQueryRepository accountQueryRepository;
    private final AdminUserQueryPort adminUserQueryPort;

    @Override
    @Transactional(readOnly = true)
    public AdminAccountDetailResponse execute(UUID accountId) {

        Account account = accountQueryRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ACCOUNT_NOT_FOUND
                        )
                );

        AdminAccountDetailResponse.UserInfo user =
                adminUserQueryPort.getUserAccountInfo(
                        account.getUserId()
                );

        return AdminAccountMapper.toDetailResponse(account, user);
    }
}