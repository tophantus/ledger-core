package com.example.ledgercore.transaction.query.mapper;

import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionQueryMapper {

    public TransactionResponse toResponse(
            MoneyTransaction transaction,
            Boolean incoming
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
                incoming,
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }

    public TransactionResponse toResponse(
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