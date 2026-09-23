package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferAccountToProviderCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferAccountToProviderUseCase;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferAccountToProviderPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferLedgerPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransferAccountToProviderHandler
        implements TransferAccountToProviderUseCase {

    private final TransactionCommandRepository transactionCommandRepository;

    private final TransferAccountToProviderPort
            transferAccountToProviderPort;

    private final TransferLedgerPort transferLedgerPort;

    private final TransactionEventPort transactionEventPort;

    private final TransactionBusinessDayPort transactionBusinessDayPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            TransferAccountToProviderCommand command
    ) {
        validateCommand(command);

        LocalDate businessDate =
                transactionBusinessDayPort.getCurrentBusinessDate();

        MoneyTransaction transaction =
                MoneyTransaction.builder()
                        .reference(command.reference())
                        .type(TransactionType.TRANSFER)
                        .status(TransactionStatus.PENDING)
                        .businessDate(businessDate)
                        .sourceAccountId(command.sourceAccountId())
                        .destinationAccountId(command.providerAccountId())
                        .amount(command.amount())
                        .currency(command.currency())
                        .description(command.description())
                        .build();

        transactionCommandRepository.save(transaction);

        transferAccountToProviderPort.decreaseSourceAccount(
                command.sourceAccountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        transferAccountToProviderPort.increaseProviderAccount(
                command.providerAccountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        transferLedgerPort.recordTransfer(
                transaction.getId(),
                command.sourceAccountId(),
                command.providerAccountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        Instant completedAt = Instant.now();

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(completedAt);

        transactionEventPort.publishTransferCompleted(
                new TransferCompletedEvent(
                        transaction.getId(),
                        transaction.getReference(),
                        transaction.getSourceAccountId(),
                        transaction.getDestinationAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        completedAt
                )
        );

        return toResponse(transaction);
    }

    private void validateCommand(
            TransferAccountToProviderCommand command
    ) {
        if (command == null
                || command.sourceAccountId() == null
                || command.providerAccountId() == null
                || command.amount() == null
                || command.currency() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.sourceAccountId()
                .equals(command.providerAccountId())) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        if (command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_AMOUNT
            );
        }

        CurrencyAmountPolicy.validate(
                command.amount(),
                command.currency()
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