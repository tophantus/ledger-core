
package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPaymentAmountResult;
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
class GetCreditPaymentAmountHandlerTest {

    @Mock
    private TransactionQueryRepository repository;

    @InjectMocks
    private GetCreditPaymentAmountHandler handler;

    private UUID creditFacilityId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private GetCreditPaymentAmountQuery query;

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();
        fromDate = LocalDate.of(2026, 1, 1);
        toDate = LocalDate.of(2026, 1, 31);

        query = new GetCreditPaymentAmountQuery(
                creditFacilityId,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldReturnTotalAmount_whenQueryIsValid() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("1500000.00");

        when(repository.sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        // Act
        GetCreditPaymentAmountResult result = handler.execute(query);

        // Assert
        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository, times(1))
                .sumCreditPaymentAmount(
                        creditFacilityId,
                        TransactionType.CREDIT_PAYMENT,
                        TransactionStatus.COMPLETED,
                        fromDate,
                        toDate
                );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_shouldReturnZero_whenNoPaymentsExist() {
        // Arrange
        BigDecimal expectedAmount = BigDecimal.ZERO;

        when(repository.sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        // Act
        GetCreditPaymentAmountResult result = handler.execute(query);

        // Assert
        assertNotNull(result);
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.totalAmount())
        );

        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());

        verify(repository).sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
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
        GetCreditPaymentAmountQuery invalidQuery =
                new GetCreditPaymentAmountQuery(
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
        GetCreditPaymentAmountQuery invalidQuery =
                new GetCreditPaymentAmountQuery(
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
        GetCreditPaymentAmountQuery invalidQuery =
                new GetCreditPaymentAmountQuery(
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
        GetCreditPaymentAmountQuery invalidQuery =
                new GetCreditPaymentAmountQuery(
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

        GetCreditPaymentAmountQuery validQuery =
                new GetCreditPaymentAmountQuery(
                        creditFacilityId,
                        date,
                        date
                );

        BigDecimal expectedAmount = new BigDecimal("500000.00");

        when(repository.sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
                TransactionStatus.COMPLETED,
                date,
                date
        )).thenReturn(expectedAmount);

        // Act
        GetCreditPaymentAmountResult result =
                handler.execute(validQuery);

        // Assert
        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(date, result.fromDate());
        assertEquals(date, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository).sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
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

        when(repository.sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
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

        verify(repository).sumCreditPaymentAmount(
                creditFacilityId,
                TransactionType.CREDIT_PAYMENT,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        );

        verifyNoMoreInteractions(repository);
    }
}