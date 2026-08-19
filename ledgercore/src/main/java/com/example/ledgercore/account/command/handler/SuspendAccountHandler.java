package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.SuspendAccountCommand;
import com.example.ledgercore.account.command.port.inbound.SuspendAccountUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuspendAccountHandler implements SuspendAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(SuspendAccountCommand command) {
        Account account = accountCommandRepository.findById(command.accountId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
                );

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_SUSPENDED);
        }

        account.setStatus(AccountStatus.SUSPENDED);

        accountCommandRepository.save(account);
    }
}