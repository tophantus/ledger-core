package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetTransactionQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.mapper.TransactionQueryMapper;
import com.example.ledgercore.transaction.query.port.outbound.TransactionAccessPort;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTransactionHandlerTest {

    @Mock
    private TransactionQueryRepository transactionQueryRepository;

    @Mock
    private TransactionAccessPort transactionAccessPort;

    @Mock
    private TransactionQueryMapper transactionQueryMapper;

    @InjectMocks
    private GetTransactionHandler handler;

    private UUID userId;
    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    private MoneyTransaction transaction;
    private TransactionResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        transaction = MoneyTransaction.builder()
                .id(transactionId)
                .reference("TXN-001")
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(new BigDecimal("100000"))
                .currency("VND")
                .description("Transfer")
                .createdAt(Instant.now())
                .build();

        response = new TransactionResponse(
                transactionId,
                "TXN-001",
                TransactionType.TRANSFER,
                TransactionStatus.COMPLETED,
                sourceAccountId,
                destinationAccountId,
                new BigDecimal("100000"),
                "VND",
                "Transfer",
                transaction.getCreatedAt(),
                null
        );
    }

    @Test
    void shouldReturnTransaction_whenUserHasAccess() {
        GetTransactionQuery query =
                new GetTransactionQuery(
                        userId,
                        transactionId
                );

        when(transactionQueryRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(transactionQueryMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                handler.execute(query);

        assertSame(response, result);

        verify(transactionQueryRepository)
                .findById(transactionId);

        verify(transactionAccessPort)
                .verifyAccess(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        verify(transactionQueryMapper)
                .toResponse(transaction);
    }

    @Test
    void shouldThrowInvalidRequest_whenQueryIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionQueryRepository,
                transactionAccessPort,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenUserIdIsNull() {
        GetTransactionQuery query =
                new GetTransactionQuery(
                        null,
                        transactionId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionQueryRepository,
                transactionAccessPort,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenTransactionIdIsNull() {
        GetTransactionQuery query =
                new GetTransactionQuery(
                        userId,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionQueryRepository,
                transactionAccessPort,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowTransactionNotFound_whenTransactionDoesNotExist() {
        GetTransactionQuery query =
                new GetTransactionQuery(
                        userId,
                        transactionId
                );

        when(transactionQueryRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.TRANSACTION_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(transactionQueryRepository)
                .findById(transactionId);

        verifyNoInteractions(
                transactionAccessPort,
                transactionQueryMapper
        );
    }

    @Test
    void shouldPropagateAccessException_whenUserHasNoAccess() {
        GetTransactionQuery query =
                new GetTransactionQuery(
                        userId,
                        transactionId
                );

        when(transactionQueryRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        BusinessException accessException =
                new BusinessException(
                        ErrorCode.ACCESS_DENIED
                );

        doThrow(accessException)
                .when(transactionAccessPort)
                .verifyAccess(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertSame(
                accessException,
                exception
        );

        verify(transactionAccessPort)
                .verifyAccess(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        verifyNoInteractions(transactionQueryMapper);
    }
}