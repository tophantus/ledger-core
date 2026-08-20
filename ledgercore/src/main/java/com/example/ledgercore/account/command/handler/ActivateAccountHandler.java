package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.ActivateAccountCommand;
import com.example.ledgercore.account.command.port.inbound.ActivateAccountUseCase;
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
public class ActivateAccountHandler implements ActivateAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(ActivateAccountCommand command) {
        Account account = accountCommandRepository.findById(command.accountId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
                );

        if (!account.getUserId().equals(command.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVE);
        }

        account.setStatus(AccountStatus.ACTIVE);

        accountCommandRepository.save(account);
    }
}