package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.RepayCreditCommand;
import com.example.ledgercore.transaction.command.port.inbound.RepayCreditUseCase;
import com.example.ledgercore.transaction.command.port.outbound.RepayCreditFacilityAccountBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditPaymentLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
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
public class RepayCreditHandler
        implements RepayCreditUseCase {

    private final TransactionCommandRepository
            transactionCommandRepository;

    private final RepayCreditFacilityAccountBalancePort
            repayCreditFacilityAccountBalancePort;

    private final CreditFacilityBalancePort
            creditFacilityBalancePort;

    private final CreditPaymentLedgerPort
            creditPaymentLedgerPort;

    private final TransactionBusinessDayPort
            transactionBusinessDayPort;

    private final TransactionEventPort
            transactionEventPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            RepayCreditCommand command
    ) {
        validateCommand(command);

        LocalDate businessDate =
                transactionBusinessDayPort.getCurrentBusinessDate();

        MoneyTransaction transaction =
                MoneyTransaction.builder()
                        .reference(command.reference())
                        .type(TransactionType.CREDIT_PAYMENT)
                        .status(TransactionStatus.PENDING)
                        .businessDate(businessDate)
                        .sourceAccountId(command.accountId())
                        .destinationCreditFacilityId(
                                command.creditFacilityId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .description(command.description())
                        .build();

        transactionCommandRepository.save(transaction);

        repayCreditFacilityAccountBalancePort.decreaseBalance(
                command.accountId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        creditFacilityBalancePort.decreaseOutstandingBalance(
                command.creditFacilityId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        creditPaymentLedgerPort.recordCreditPayment(
                transaction.getId(),
                command.creditFacilityId(),
                command.accountId(),
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

        transactionEventPort.publishAccountBalanceChanged(
                new AccountBalanceChangedEvent(
                        transaction.getId(),
                        command.accountId(),
                        command.amount().negate(),
                        command.currency(),
                        completedAt
                )
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

        return toResponse(transaction);
    }

    private void validateCommand(
            RepayCreditCommand command
    ) {
        if (command == null
                || command.accountId() == null
                || command.creditFacilityId() == null
                || command.amount() == null
                || command.currency() == null
                || command.reference() == null
                || command.reference().isBlank()) {

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