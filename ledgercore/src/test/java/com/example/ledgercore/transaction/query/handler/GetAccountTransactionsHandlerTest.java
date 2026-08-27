package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetAccountTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.mapper.TransactionQueryMapper;
import com.example.ledgercore.transaction.query.port.outbound.TransactionAccessPort;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountTransactionsHandlerTest {

    @Mock
    private TransactionQueryRepository transactionQueryRepository;

    @Mock
    private TransactionAccessPort transactionAccessPort;

    @Mock
    private TransactionQueryMapper transactionQueryMapper;

    @InjectMocks
    private GetAccountTransactionsHandler handler;

    private UUID userId;
    private UUID accountId;

    private MoneyTransaction transaction1;
    private MoneyTransaction transaction2;

    private TransactionResponse response1;
    private TransactionResponse response2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        transaction1 = MoneyTransaction.builder()
                .id(UUID.randomUUID())
                .reference("TXN-001")
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(accountId)
                .destinationAccountId(UUID.randomUUID())
                .amount(new BigDecimal("100000"))
                .currency("VND")
                .description("Transfer")
                .createdAt(Instant.now())
                .build();

        transaction2 = MoneyTransaction.builder()
                .id(UUID.randomUUID())
                .reference("TXN-002")
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(null)
                .destinationAccountId(accountId)
                .amount(new BigDecimal("500000"))
                .currency("VND")
                .description("Deposit")
                .createdAt(Instant.now())
                .build();

        response1 = mock(TransactionResponse.class);
        response2 = mock(TransactionResponse.class);
    }

    @Test
    void shouldReturnPagedTransactions() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        Page<MoneyTransaction> page =
                new PageImpl<>(
                        List.of(
                                transaction1,
                                transaction2
                        ),
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        ),
                        2
                );

        when(transactionQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(transactionQueryMapper.toResponse(transaction1))
                .thenReturn(response1);

        when(transactionQueryMapper.toResponse(transaction2))
                .thenReturn(response2);

        PageResponse<TransactionResponse> result =
                handler.execute(query);

        assertEquals(
                List.of(response1, response2),
                result.content()
        );

        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);

        verify(transactionQueryMapper)
                .toResponse(transaction1);

        verify(transactionQueryMapper)
                .toResponse(transaction2);
    }

    @Test
    void shouldVerifyAccountAccessBeforeQueryingTransactions() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        mockRepositoryResult();

        handler.execute(query);

        var inOrder = inOrder(
                transactionAccessPort,
                transactionQueryRepository
        );

        inOrder.verify(transactionAccessPort)
                .verifyAccess(userId, accountId);

        inOrder.verify(transactionQueryRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldCreateCorrectPageable() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        2,
                        10
                );

        Page<MoneyTransaction> page =
                new PageImpl<>(
                        List.of(),
                        org.springframework.data.domain.PageRequest.of(
                                2,
                                10
                        ),
                        25
                );

        when(transactionQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        handler.execute(query);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionQueryRepository)
                .findAll(
                        any(Specification.class),
                        captor.capture()
                );

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                "createdAt",
                pageable.getSort()
                        .iterator()
                        .next()
                        .getProperty()
        );

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getSort()
                        .iterator()
                        .next()
                        .getDirection()
        );

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);
    }

    @Test
    void shouldAcceptStatusFilter() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        TransactionStatus.COMPLETED,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        mockRepositoryResult();

        assertDoesNotThrow(
                () -> handler.execute(query)
        );

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);

        verify(transactionQueryRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldAcceptTypeFilter() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        TransactionType.TRANSFER,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        mockRepositoryResult();

        assertDoesNotThrow(
                () -> handler.execute(query)
        );

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);
    }

    @Test
    void shouldAcceptCurrencyFilter() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        "VND",
                        null,
                        null,
                        0,
                        20
                );

        mockRepositoryResult();

        assertDoesNotThrow(
                () -> handler.execute(query)
        );

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);
    }

    @Test
    void shouldAcceptDateRangeFilter() {
        Instant from = Instant.parse(
                "2026-08-01T00:00:00Z"
        );

        Instant to = Instant.parse(
                "2026-08-31T23:59:59Z"
        );

        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        from,
                        to,
                        0,
                        20
                );

        mockRepositoryResult();

        assertDoesNotThrow(
                () -> handler.execute(query)
        );

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenUserIdIsNull() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        null,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenAccountIdIsNull() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenPageIsNegative() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        -1,
                        20
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenSizeIsZero() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        0
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenSizeExceedsLimit() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        101
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenFromIsAfterTo() {
        Instant from = Instant.parse(
                "2026-08-31T00:00:00Z"
        );

        Instant to = Instant.parse(
                "2026-08-01T00:00:00Z"
        );

        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        from,
                        to,
                        0,
                        20
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldThrowInvalidRequest_whenCurrencyIsBlank() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        "   ",
                        null,
                        null,
                        0,
                        20
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
                transactionAccessPort,
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    @Test
    void shouldNotQueryTransactions_whenAccessIsDenied() {
        GetAccountTransactionsQuery query =
                new GetAccountTransactionsQuery(
                        userId,
                        accountId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        doThrow(
                new BusinessException(
                        ErrorCode.ACCESS_DENIED
                )
        ).when(transactionAccessPort)
                .verifyAccess(userId, accountId);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(transactionAccessPort)
                .verifyAccess(userId, accountId);

        verifyNoInteractions(
                transactionQueryRepository,
                transactionQueryMapper
        );
    }

    private void mockRepositoryResult() {
        when(transactionQueryRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(
                        List.of()
                )
        );
    }
}