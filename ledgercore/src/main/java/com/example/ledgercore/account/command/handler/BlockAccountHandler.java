package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.BlockAccountCommand;
import com.example.ledgercore.account.command.port.inbound.BlockAccountUseCase;
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
public class BlockAccountHandler implements BlockAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(BlockAccountCommand command) {
        Account account = accountCommandRepository.findById(command.accountId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
                );

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_BLOCKED);
        }

        account.setStatus(AccountStatus.BLOCKED);

        accountCommandRepository.save(account);
    }
}