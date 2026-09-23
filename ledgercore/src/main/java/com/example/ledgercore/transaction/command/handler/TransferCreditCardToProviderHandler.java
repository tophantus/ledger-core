package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferCreditCardToProviderCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferCreditCardToProviderUseCase;
import com.example.ledgercore.transaction.command.port.outbound.*;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
import com.example.ledgercore.transaction.event.CreditFacilityBalanceChangedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransferCreditCardToProviderHandler
        implements TransferCreditCardToProviderUseCase {

    private final TransactionCommandRepository
            transactionCommandRepository;

    private final TransferCreditFacilityToProviderPort
            transferCreditFacilityToProviderPort;

    private final CreditPaymentLedgerPort creditPaymentLedgerPort;

    private final TransactionBusinessDayPort transactionBusinessDayPort;

    private final TransactionEventPort transactionEventPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            TransferCreditCardToProviderCommand command
    ) {
        validateCommand(command);

        LocalDate businessDate =
                transactionBusinessDayPort.getCurrentBusinessDate();

        MoneyTransaction transaction =
                MoneyTransaction.builder()
                        .reference(command.reference())
                        .type(TransactionType.CARD_PAYMENT)
                        .status(TransactionStatus.PENDING)
                        .businessDate(businessDate)
                        .sourceCreditFacilityId(
                                command.creditFacilityId()
                        )
                        .destinationAccountId(
                                command.providerAccountId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .description(command.description())
                        .build();

        transactionCommandRepository.save(transaction);

        transferCreditFacilityToProviderPort
                .decreaseCreditFacilityOutstandingBalance(
                        command.creditFacilityId(),
                        command.amount(),
                        command.currency(),
                        businessDate
                );

        transferCreditFacilityToProviderPort
                .increaseProviderAccount(
                        command.providerAccountId(),
                        command.amount(),
                        command.currency(),
                        businessDate
                );

        creditPaymentLedgerPort.recordCreditPayment(
                transaction.getId(),
                command.creditFacilityId(),
                command.providerAccountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        Instant completedAt = Instant.now();

        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        transaction.setCompletedAt(
                completedAt
        );

        transactionEventPort.publishCreditFacilityBalanceChanged(
                new CreditFacilityBalanceChangedEvent(
                        transaction.getId(),
                        command.creditFacilityId(),
                        command.amount().negate(),
                        command.currency(),
                        completedAt
                )
        );

        transactionEventPort.publishAccountBalanceChanged(
                new AccountBalanceChangedEvent(
                        transaction.getId(),
                        command.providerAccountId(),
                        command.amount(),
                        command.currency(),
                        completedAt
                )
        );

        return toResponse(transaction);
    }

    private void validateCommand(
            TransferCreditCardToProviderCommand command
    ) {
        if (command == null
                || command.creditFacilityId() == null
                || command.providerAccountId() == null
                || command.amount() == null
                || command.currency() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
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