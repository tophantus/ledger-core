package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.ActivateUserAccountCommand;
import com.example.ledgercore.account.command.port.inbound.ActivateUserAccountUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateUserAccountHandler implements ActivateUserAccountUseCase {

    private final GetUserAccountService getUserAccountService;
    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(ActivateUserAccountCommand command) {
        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountCriteria(command.accountId())
        );

        if (!account.userId().equals(command.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (account.status() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        if (account.status() == AccountStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVE);
        }

        accountCommandRepository.updateStatus(
                account.accountId(),
                AccountStatus.ACTIVE
        );
    }
}