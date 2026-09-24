package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferMoneyUseCase;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
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
public class TransferMoneyHandler
        implements TransferMoneyUseCase {

    private final TransactionCommandRepository
            transactionCommandRepository;

    private final TransferUserAccountPort
            transferUserAccountPort;

    private final TransferLedgerPort
            transferLedgerPort;

    private final TransactionEventPort
            transactionEventPort;

    private final TransactionBusinessDayPort
            transactionBusinessDayPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            TransferMoneyCommand command
    ) {
        validateCommand(command);

        LocalDate businessDate =
                transactionBusinessDayPort
                        .getCurrentBusinessDate();

        MoneyTransaction transaction =
                createTransaction(
                        command,
                        businessDate
                );

        transactionCommandRepository.save(transaction);

        transferUserAccountPort.transfer(
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                businessDate
        );

        transferLedgerPort.recordTransfer(
                transaction.getId(),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        Instant completedAt = Instant.now();

        completeTransaction(
                transaction,
                completedAt
        );

        publishBalanceChangedEvents(
                transaction,
                completedAt
        );

        return toResponse(transaction);
    }

    private void validateCommand(
            TransferMoneyCommand command
    ) {
        if (command == null
                || command.sourceAccountId() == null
                || command.destinationAccountId() == null
                || command.amount() == null
                || command.currency() == null
                || command.reference() == null
                || command.reference().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.sourceAccountId()
                .equals(command.destinationAccountId())) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        if (command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }

        CurrencyAmountPolicy.validate(
                command.amount(),
                command.currency()
        );
    }

    private MoneyTransaction createTransaction(
            TransferMoneyCommand command,
            LocalDate businessDate
    ) {
        return MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .businessDate(businessDate)
                .sourceAccountId(
                        command.sourceAccountId()
                )
                .destinationAccountId(
                        command.destinationAccountId()
                )
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .build();
    }

    private void completeTransaction(
            MoneyTransaction transaction,
            Instant completedAt
    ) {
        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        transaction.setCompletedAt(
                completedAt
        );
    }

    private void publishBalanceChangedEvents(
            MoneyTransaction transaction,
            Instant completedAt
    ) {
        transactionEventPort.publishAccountBalanceChanged(
                new AccountBalanceChangedEvent(
                        transaction.getId(),
                        transaction.getSourceAccountId(),
                        transaction.getAmount().negate(),
                        transaction.getCurrency(),
                        completedAt
                )
        );

        transactionEventPort.publishAccountBalanceChanged(
                new AccountBalanceChangedEvent(
                        transaction.getId(),
                        transaction.getDestinationAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        completedAt
                )
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