package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.query.dto.GetTransactionByReferenceQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.mapper.TransactionQueryMapper;
import com.example.ledgercore.transaction.query.port.inbound.GetTransactionByReferenceUseCase;
import com.example.ledgercore.transaction.query.port.outbound.TransactionAccessPort;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTransactionByReferenceHandler
        implements GetTransactionByReferenceUseCase {

    private final TransactionQueryRepository transactionQueryRepository;
    private final TransactionAccessPort transactionAccessPort;
    private final TransactionQueryMapper transactionQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse execute(
            GetTransactionByReferenceQuery query
    ) {
        validateQuery(query);

        MoneyTransaction transaction =
                transactionQueryRepository
                        .findByReference(query.reference())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TRANSACTION_NOT_FOUND
                                )
                        );

        transactionAccessPort.verifyAccess(
                query.userId(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId()
        );

        return transactionQueryMapper.toResponse(transaction);
    }

    private void validateQuery(
            GetTransactionByReferenceQuery query
    ) {
        if (query == null
                || query.userId() == null
                || query.reference() == null
                || query.reference().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}