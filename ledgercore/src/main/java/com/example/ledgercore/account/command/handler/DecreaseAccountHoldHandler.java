package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DecreaseAccountHoldCommand;
import com.example.ledgercore.account.command.port.inbound.DecreaseAccountHoldUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DecreaseAccountHoldHandler
        implements DecreaseAccountHoldUseCase {

    private final AccountCommandRepository
            accountCommandRepository;

    @Override
    @Transactional
    public void execute(
            DecreaseAccountHoldCommand command
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

        if (account.getCurrency() != (command.currency())) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_CURRENCY_MISMATCH
            );
        }

        if (account.getHoldAmount().compareTo(
                command.amount()
        ) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_HOLD_AMOUNT_INSUFFICIENT
            );
        }

        account.setHoldAmount(
                account.getHoldAmount()
                        .subtract(command.amount())
        );
    }

    private void validateCommand(
            DecreaseAccountHoldCommand command
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