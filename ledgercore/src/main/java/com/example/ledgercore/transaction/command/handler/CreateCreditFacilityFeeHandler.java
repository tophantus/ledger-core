package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.CreateCreditFacilityFeeCommand;
import com.example.ledgercore.transaction.command.port.inbound.CreateCreditFacilityFeeUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditFeeLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateCreditFacilityFeeHandler
        implements CreateCreditFacilityFeeUseCase {

    private final TransactionCommandRepository
            transactionCommandRepository;

    private final CreditFacilityBalancePort
            creditFacilityBalancePort;

    private final CreditFeeLedgerPort
            creditFeeLedgerPort;

    private final TransactionBusinessDayPort
            transactionBusinessDayPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            CreateCreditFacilityFeeCommand command
    ) {
        validateCommand(command);

        LocalDate businessDate =
                transactionBusinessDayPort.getCurrentBusinessDate();

        MoneyTransaction transaction =
                MoneyTransaction.builder()
                        .reference(command.reference())
                        .type(TransactionType.FEE)
                        .status(TransactionStatus.PENDING)
                        .businessDate(businessDate)
                        .sourceCreditFacilityId(
                                command.creditFacilityId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .description(command.description())
                        .build();

        transactionCommandRepository.save(transaction);

        creditFacilityBalancePort.increaseOutstandingBalance(
                command.creditFacilityId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        creditFeeLedgerPort.recordCreditFee(
                transaction.getId(),
                command.creditFacilityId(),
                command.amount(),
                command.currency(),
                businessDate
        );

        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        transaction.setCompletedAt(
                Instant.now()
        );

        return toResponse(transaction);
    }

    private void validateCommand(
            CreateCreditFacilityFeeCommand command
    ) {
        if (command == null
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