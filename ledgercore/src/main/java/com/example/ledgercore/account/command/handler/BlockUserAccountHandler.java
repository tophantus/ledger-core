package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.BlockUserAccountCommand;
import com.example.ledgercore.account.command.port.inbound.BlockUserAccountUseCase;
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

@Service
@RequiredArgsConstructor
public class BlockUserAccountHandler implements BlockUserAccountUseCase {

    private final GetUserAccountService getUserAccountService;
    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(BlockUserAccountCommand command) {
        GetUserAccountResult account = getUserAccountService.execute(
                new GetUserAccountQuery(command.accountId())
        );

        if (!account.userId().equals(command.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (account.status() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        if (account.status() == AccountStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_BLOCKED);
        }

        accountCommandRepository.updateStatus(
                account.accountId(),
                AccountStatus.BLOCKED
        );
    }
}