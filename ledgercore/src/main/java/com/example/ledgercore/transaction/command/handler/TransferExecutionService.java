package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerTransferPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferExecutionService {

    private final TransactionCommandRepository transactionCommandRepository;
    private final AccountTransferPort accountTransferPort;
    private final LedgerTransferPort ledgerTransferPort;

    @Transactional
    public TransactionResponse execute(
            UUID userId,
            TransferMoneyCommand command,
            UUID destinationAccountId
    ) {
        AccountTransferPort.TransferAccountInfo transferInfo =
                accountTransferPort.getTransferInfo(
                        userId,
                        command.sourceAccountId(),
                        destinationAccountId
                );

        validateTransfer(
                command,
                transferInfo
        );

        MoneyTransaction transaction =
                createTransaction(
                        command,
                        transferInfo
                );

        transactionCommandRepository.save(transaction);

        accountTransferPort.transfer(
                transferInfo.sourceAccountId(),
                transferInfo.destinationAccountId(),
                command.amount()
        );

        ledgerTransferPort.recordTransfer(
                transaction.getId(),
                transferInfo.sourceAccountId(),
                transferInfo.destinationAccountId(),
                command.amount(),
                command.currency()
        );

        completeTransaction(transaction);

        return toResponse(transaction);
    }

    private void validateTransfer(
            TransferMoneyCommand command,
            AccountTransferPort.TransferAccountInfo transferInfo
    ) {
        if (!transferInfo.currency().equals(command.currency())) {
            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }

        if (transferInfo.sourceBalance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }

    private MoneyTransaction createTransaction(
            TransferMoneyCommand command,
            AccountTransferPort.TransferAccountInfo transferInfo
    ) {
        return MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .sourceAccountId(
                        transferInfo.sourceAccountId()
                )
                .destinationAccountId(
                        transferInfo.destinationAccountId()
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
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}