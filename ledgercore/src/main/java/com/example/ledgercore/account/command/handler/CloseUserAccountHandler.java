package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CloseUserAccountCommand;
import com.example.ledgercore.account.command.port.inbound.CloseUserAccountUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.service.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CloseUserAccountHandler implements CloseUserAccountUseCase {

    private final GetUserAccountService getUserAccountService;
    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(CloseUserAccountCommand command) {
        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountQuery(command.accountId())
        );

        if (!account.userId().equals(command.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (account.status() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        if (account.balance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_BALANCE_NOT_ZERO);
        }

        accountCommandRepository.updateStatus(
                account.accountId(),
                AccountStatus.CLOSED
        );
    }
}