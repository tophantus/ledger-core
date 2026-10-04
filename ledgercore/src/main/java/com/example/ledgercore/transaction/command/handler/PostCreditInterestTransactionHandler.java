package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.PostCreditInterestTransactionCommand;
import com.example.ledgercore.transaction.command.port.inbound.PostCreditInterestTransactionUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditInterestLedgerPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PostCreditInterestTransactionHandler
        implements PostCreditInterestTransactionUseCase {

    private final TransactionCommandRepository
            transactionCommandRepository;

    private final CreditFacilityBalancePort
            creditFacilityBalancePort;

    private final CreditInterestLedgerPort
            creditInterestLedgerPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            PostCreditInterestTransactionCommand command
    ) {
        validateCommand(command);

        String reference =
                buildReference(command);

        MoneyTransaction transaction =
                MoneyTransaction.builder()
                        .reference(reference)
                        .type(TransactionType.INTEREST)
                        .status(TransactionStatus.PENDING)
                        .businessDate(command.businessDate())
                        .sourceCreditFacilityId(
                                command.creditFacilityId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .description("Credit interest posting")
                        .build();

        transactionCommandRepository.save(transaction);

        creditFacilityBalancePort.increaseOutstandingBalance(
                command.creditFacilityId(),
                command.amount(),
                command.currency(),
                command.businessDate()
        );

        creditInterestLedgerPort.recordInterestPosting(
                transaction.getId(),
                command.creditFacilityId(),
                command.amount(),
                command.currency(),
                command.businessDate()
        );

        completeTransaction(transaction);

        return toResponse(transaction);
    }

    private void validateCommand(
            PostCreditInterestTransactionCommand command
    ) {
        if (command == null
                || command.creditFacilityId() == null
                || command.currency() == null
                || command.businessDate() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }

        CurrencyAmountPolicy.validate(
                command.amount(),
                command.currency()
        );
    }

    private String buildReference(
            PostCreditInterestTransactionCommand command
    ) {
        return "CREDIT-INTEREST-"
                + command.creditFacilityId()
                + "-"
                + command.businessDate();
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