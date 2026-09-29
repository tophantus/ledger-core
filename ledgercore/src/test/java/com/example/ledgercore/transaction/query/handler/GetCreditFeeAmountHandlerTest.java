
package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditFeeAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditFeeAmountResult;
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
class GetCreditFeeAmountHandlerTest {

    @Mock
    private TransactionQueryRepository repository;

    @InjectMocks
    private GetCreditFeeAmountHandler handler;

    private UUID creditFacilityId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private GetCreditFeeAmountQuery query;

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();
        fromDate = LocalDate.of(2026, 1, 1);
        toDate = LocalDate.of(2026, 1, 31);

        query = new GetCreditFeeAmountQuery(
                creditFacilityId,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldReturnTotalAmount_whenQueryIsValid() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("250000.00");

        when(repository.sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        // Act
        GetCreditFeeAmountResult result = handler.execute(query);

        // Assert
        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository, times(1))
                .sumCreditFeeAmount(
                        creditFacilityId,
                        TransactionType.FEE,
                        TransactionStatus.COMPLETED,
                        fromDate,
                        toDate
                );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_shouldReturnZero_whenNoFeesExist() {
        // Arrange
        BigDecimal expectedAmount = BigDecimal.ZERO;

        when(repository.sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        // Act
        GetCreditFeeAmountResult result = handler.execute(query);

        // Assert
        assertNotNull(result);
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.totalAmount())
        );

        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());

        verify(repository).sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldThrowException_whenQueryIsNull() {
        // Act & Assert
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
        // Arrange
        GetCreditFeeAmountQuery invalidQuery =
                new GetCreditFeeAmountQuery(
                        null,
                        fromDate,
                        toDate
                );

        // Act & Assert
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
        // Arrange
        GetCreditFeeAmountQuery invalidQuery =
                new GetCreditFeeAmountQuery(
                        creditFacilityId,
                        null,
                        toDate
                );

        // Act & Assert
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
        // Arrange
        GetCreditFeeAmountQuery invalidQuery =
                new GetCreditFeeAmountQuery(
                        creditFacilityId,
                        fromDate,
                        null
                );

        // Act & Assert
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
        // Arrange
        GetCreditFeeAmountQuery invalidQuery =
                new GetCreditFeeAmountQuery(
                        creditFacilityId,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 31)
                );

        // Act & Assert
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
        // Arrange
        LocalDate date = LocalDate.of(2026, 1, 15);

        GetCreditFeeAmountQuery validQuery =
                new GetCreditFeeAmountQuery(
                        creditFacilityId,
                        date,
                        date
                );

        BigDecimal expectedAmount = new BigDecimal("100000.00");

        when(repository.sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                date,
                date
        )).thenReturn(expectedAmount);

        // Act
        GetCreditFeeAmountResult result =
                handler.execute(validQuery);

        // Assert
        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(date, result.fromDate());
        assertEquals(date, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository).sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                date,
                date
        );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_shouldPropagateException_whenRepositoryFails() {
        // Arrange
        RuntimeException expectedException =
                new RuntimeException("Database error");

        when(repository.sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenThrow(expectedException);

        // Act
        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> handler.execute(query)
        );

        // Assert
        assertSame(expectedException, actualException);

        verify(repository).sumCreditFeeAmount(
                creditFacilityId,
                TransactionType.FEE,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        );

        verifyNoMoreInteractions(repository);
    }
}