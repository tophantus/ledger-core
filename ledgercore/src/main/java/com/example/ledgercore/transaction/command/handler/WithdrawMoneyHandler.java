package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.WithdrawMoneyUseCase;
import com.example.ledgercore.transaction.command.port.outbound.WithdrawUserAccountPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.WithdrawLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WithdrawMoneyHandler
        implements WithdrawMoneyUseCase {

    private final TransactionCommandRepository transactionCommandRepository;
    private final WithdrawUserAccountPort withdrawUserAccountPort;
    private final WithdrawLedgerPort withdrawLedgerPort;
    private final TransactionEventPort transactionEventPort;
    private final TransactionBusinessDayPort transactionBusinessDayPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            WithdrawMoneyCommand command
    ) {
        validateAmount(command);

        LocalDate businessDate =
                transactionBusinessDayPort.getCurrentBusinessDate();

        MoneyTransaction transaction =
                createTransaction(
                        command,
                        businessDate
                );

        transactionCommandRepository.save(transaction);

        withdrawUserAccountPort.withdraw(
                command.sourceAccountId(),
                command.amount(),
                businessDate
        );

        withdrawLedgerPort.recordWithdraw(
                transaction.getId(),
                command.sourceAccountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        completeTransaction(transaction);

        transactionEventPort.publishAccountBalanceChanged(
                new AccountBalanceChangedEvent(
                        transaction.getId(),
                        transaction.getSourceAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        Instant.now()
                )
        );

        return toResponse(transaction);
    }

    private void validateAmount(
            WithdrawMoneyCommand command
    ) {
        if (command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_WITHDRAW_AMOUNT
            );
        }

        CurrencyAmountPolicy.validate(
                command.amount(),
                command.currency()
        );
    }

    private MoneyTransaction createTransaction(
            WithdrawMoneyCommand command,
            LocalDate businessDate

    ) {
        return MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.PENDING)
                .businessDate(businessDate)
                .sourceAccountId(
                        command.sourceAccountId()
                )
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .build();
    }

    private void completeTransaction(
            MoneyTransaction transaction
    ) {
        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        transaction.setCompletedAt(
                Instant.now()
        );
    }

    private TransactionResponse toResponse(
            MoneyTransaction transaction
    ) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                null,
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}