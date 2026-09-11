package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.IncreaseAccountHoldCommand;
import com.example.ledgercore.account.command.port.inbound.IncreaseAccountHoldUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IncreaseAccountHoldHandler
        implements IncreaseAccountHoldUseCase {

    private final AccountCommandRepository
            accountCommandRepository;

    @Override
    @Transactional
    public void execute(
            IncreaseAccountHoldCommand command
    ) {
        validateCommand(command);

        Account account =
                accountCommandRepository
                        .findById(command.accountId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        if (account.getCurrency() != (command.currency())) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_CURRENCY_MISMATCH
            );
        }

        BigDecimal availableBalance =
                account.getBalance()
                        .subtract(account.getHoldAmount());

        if (availableBalance.compareTo(
                command.amount()
        ) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }

        account.setHoldAmount(
                account.getHoldAmount()
                        .add(command.amount())
        );
    }

    private void validateCommand(
            IncreaseAccountHoldCommand command
    ) {
        if (command == null
                || command.accountId() == null
                || command.amount() == null
                || command.currency() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount().compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_HOLD_AMOUNT
            );
        }
    }
}