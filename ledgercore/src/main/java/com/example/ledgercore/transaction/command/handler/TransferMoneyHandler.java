package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferMoneyUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferMoneyHandler
        implements TransferMoneyUseCase {

    private final TransactionCommandRepository transactionCommandRepository;
    private final AccountTransferPort accountTransferPort;
    private final TransferExecutionService transferExecutionService;

    @Override
    @Transactional
    public TransactionResponse execute(
            UUID userId,
            TransferMoneyCommand command
    ) {
        validateAmount(command);

        MoneyTransaction existingTransaction =
                transactionCommandRepository
                        .findByReference(command.reference())
                        .orElse(null);

        if (existingTransaction != null) {
            return handleExistingTransaction(
                    userId,
                    existingTransaction
            );
        }

        UUID destinationAccountId =
                accountTransferPort.getAccountIdByAccountNo(
                        command.destinationAccountNo()
                );

        if (command.sourceAccountId()
                .equals(destinationAccountId)) {
            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        return transferExecutionService.execute(
                userId,
                command,
                destinationAccountId
        );
    }

    private void validateAmount(
            TransferMoneyCommand command
    ) {
        if (command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }
    }

    private TransactionResponse handleExistingTransaction(
            UUID userId,
            MoneyTransaction transaction
    ) {
        accountTransferPort.verifySourceAccountAccess(
                userId,
                transaction.getSourceAccountId()
        );

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