
package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountQuery;
import com.example.ledgercore.transaction.query.dto.GetCreditPurchasesAmountResult;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCreditPurchasesAmountHandlerTest {

    @Mock
    private TransactionQueryRepository repository;

    @InjectMocks
    private GetCreditPurchasesAmountHandler handler;

    private UUID creditFacilityId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private GetCreditPurchasesAmountQuery query;

    private static final List<TransactionType> PURCHASE_TYPES =
            List.of(
                    TransactionType.CARD_PURCHASE,
                    TransactionType.CASH_ADVANCE
            );

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();
        fromDate = LocalDate.of(2026, 1, 1);
        toDate = LocalDate.of(2026, 1, 31);

        query = new GetCreditPurchasesAmountQuery(
                creditFacilityId,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldReturnTotalAmount_whenQueryIsValid() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("1500000.00");

        when(repository.sumCreditFacilityPurchasesAmount(
                creditFacilityId,
                PURCHASE_TYPES,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        // Act
        GetCreditPurchasesAmountResult result = handler.execute(query);

        // Assert
        assertNotNull(result);
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());
        assertEquals(expectedAmount, result.totalAmount());

        verify(repository, times(1))
                .sumCreditFacilityPurchasesAmount(
                        creditFacilityId,
                        PURCHASE_TYPES,
                        TransactionStatus.COMPLETED,
                        fromDate,
                        toDate
                );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_shouldReturnZero_whenNoPurchasesExist() {
        // Arrange
        BigDecimal expectedAmount = BigDecimal.ZERO;

        when(repository.sumCreditFacilityPurchasesAmount(
                creditFacilityId,
                PURCHASE_TYPES,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        )).thenReturn(expectedAmount);

        // Act
        GetCreditPurchasesAmountResult result = handler.execute(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalAmount()));

        verify(repository).sumCreditFacilityPurchasesAmount(
                creditFacilityId,
                PURCHASE_TYPES,
                TransactionStatus.COMPLETED,
                fromDate,
                toDate
        );
    }

    @Test
    void execute_shouldThrowException_whenQueryIsNull() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenCreditFacilityIdIsNull() {
        // Arrange
        GetCreditPurchasesAmountQuery invalidQuery =
                new GetCreditPurchasesAmountQuery(
                        null,
                        fromDate,
                        toDate
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenFromDateIsNull() {
        // Arrange
        GetCreditPurchasesAmountQuery invalidQuery =
                new GetCreditPurchasesAmountQuery(
                        creditFacilityId,
                        null,
                        toDate
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenToDateIsNull() {
        // Arrange
        GetCreditPurchasesAmountQuery invalidQuery =
                new GetCreditPurchasesAmountQuery(
                        creditFacilityId,
                        fromDate,
                        null
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldThrowException_whenFromDateIsAfterToDate() {
        // Arrange
        GetCreditPurchasesAmountQuery invalidQuery =
                new GetCreditPurchasesAmountQuery(
                        creditFacilityId,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 31)
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(invalidQuery)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void execute_shouldAcceptSameFromDateAndToDate() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 1, 15);
        GetCreditPurchasesAmountQuery validQuery =
                new GetCreditPurchasesAmountQuery(
                        creditFacilityId,
                        date,
                        date
                );

        BigDecimal expectedAmount = new BigDecimal("500000.00");

        when(repository.sumCreditFacilityPurchasesAmount(
                creditFacilityId,
                PURCHASE_TYPES,
                TransactionStatus.COMPLETED,
                date,
                date
        )).thenReturn(expectedAmount);

        // Act
        GetCreditPurchasesAmountResult result =
                handler.execute(validQuery);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAmount, result.totalAmount());
        assertEquals(date, result.fromDate());
        assertEquals(date, result.toDate());

        verify(repository).sumCreditFacilityPurchasesAmount(
                creditFacilityId,
                PURCHASE_TYPES,
                TransactionStatus.COMPLETED,
                date,
                date
        );
    }
}