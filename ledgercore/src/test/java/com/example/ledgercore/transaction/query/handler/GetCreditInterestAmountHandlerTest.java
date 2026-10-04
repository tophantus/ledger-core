package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditInterestAmountResult;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCreditInterestAmountHandlerTest {

    @Mock
    private TransactionQueryRepository repository;

    @InjectMocks
    private GetCreditInterestAmountHandler handler;

    private UUID creditFacilityId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private GetCreditInterestAmountQuery query;

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();
        fromDate = LocalDate.of(2026, 1, 1);
        toDate = LocalDate.of(2026, 1, 31);

        query = new GetCreditInterestAmountQuery(
                creditFacilityId,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldReturnTotalAmount_whenQueryIsValid() {
        BigDecimal expectedAmount = new BigDecimal("250000.00");

        when(repository.sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        GetCreditInterestAmountResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository, times(1))
                .sumCreditInterestAmount(
                        creditFacilityId,
                        TransactionType.INTEREST,
                        TransactionStatus.COMPLETED,
                        fromDate,
                        toDate
                );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_shouldReturnZero_whenNoInterestsExist() {
        BigDecimal expectedAmount = BigDecimal.ZERO;

        when(repository.sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        GetCreditInterestAmountResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.totalAmount())
        );

        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());

        verify(repository).sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldThrowException_whenQueryIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(null)
        );

        assertEquals(
                "query must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenCreditFacilityIdIsNull() {
        GetCreditInterestAmountQuery invalidQuery =
                new GetCreditInterestAmountQuery(
                        null,
                        fromDate,
                        toDate
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        assertEquals(
                "creditFacilityId must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenFromDateIsNull() {
        GetCreditInterestAmountQuery invalidQuery =
                new GetCreditInterestAmountQuery(
                        creditFacilityId,
                        null,
                        toDate
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        assertEquals(
                "fromDate must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenToDateIsNull() {
        GetCreditInterestAmountQuery invalidQuery =
                new GetCreditInterestAmountQuery(
                        creditFacilityId,
                        fromDate,
                        null
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        assertEquals(
                "toDate must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenFromDateIsAfterToDate() {
        GetCreditInterestAmountQuery invalidQuery =
                new GetCreditInterestAmountQuery(
                        creditFacilityId,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 31)
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        assertEquals(
                "fromDate must not be after toDate",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldAcceptSameFromDateAndToDate() {
        LocalDate date = LocalDate.of(2026, 1, 15);

        GetCreditInterestAmountQuery validQuery =
                new GetCreditInterestAmountQuery(
                        creditFacilityId,
                        date,
                        date
                );

        BigDecimal expectedAmount = new BigDecimal("100000.00");

        when(repository.sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                date,
                date
        )).thenReturn(expectedAmount);

        GetCreditInterestAmountResult result =
                handler.execute(validQuery);

        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(date, result.fromDate());
        assertEquals(date, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository).sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                date,
                date
        );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_shouldPropagateException_whenRepositoryFails() {
        RuntimeException expectedException =
                new RuntimeException("Database error");

        when(repository.sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> handler.execute(query)
        );

        assertSame(expectedException, actualException);

        verify(repository).sumCreditInterestAmount(
                creditFacilityId,
                TransactionType.INTEREST,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        );

        verifyNoMoreInteractions(repository);
    }
}
