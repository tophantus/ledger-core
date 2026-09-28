package com.example.ledgercore.interest.query.handler;

import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalQuery;
import com.example.ledgercore.interest.query.dto.GetCreditInterestAccrualTotalResult;
import com.example.ledgercore.interest.query.repository.CreditInterestAccrualQueryRepository;
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
class GetCreditInterestAccrualTotalHandlerTest {

    @Mock
    private CreditInterestAccrualQueryRepository repository;

    @InjectMocks
    private GetCreditInterestAccrualTotalHandler handler;

    @Test
    void shouldReturnTotalInterestAmount() {
        UUID creditFacilityId = UUID.randomUUID();
        LocalDate fromDate = LocalDate.of(2026, 9, 1);
        LocalDate toDate = LocalDate.of(2026, 9, 30);
        BigDecimal total = new BigDecimal("123.45");

        GetCreditInterestAccrualTotalQuery query =
                new GetCreditInterestAccrualTotalQuery(
                        creditFacilityId,
                        fromDate,
                        toDate
                );

        when(repository
                .sumInterestAmountByCreditFacilityIdAndBusinessDateBetween(
                        creditFacilityId,
                        fromDate,
                        toDate
                ))
                .thenReturn(total);

        GetCreditInterestAccrualTotalResult result =
                handler.execute(query);

        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(fromDate, result.fromDate());
        assertEquals(toDate, result.toDate());
        assertEquals(total, result.totalInterestAmount());

        verify(repository)
                .sumInterestAmountByCreditFacilityIdAndBusinessDateBetween(
                        creditFacilityId,
                        fromDate,
                        toDate
                );
    }

    @Test
    void shouldReturnZeroWhenRepositoryReturnsNull() {
        UUID creditFacilityId = UUID.randomUUID();
        LocalDate fromDate = LocalDate.of(2026, 9, 1);
        LocalDate toDate = LocalDate.of(2026, 9, 30);

        GetCreditInterestAccrualTotalQuery query =
                new GetCreditInterestAccrualTotalQuery(
                        creditFacilityId,
                        fromDate,
                        toDate
                );

        when(repository
                .sumInterestAmountByCreditFacilityIdAndBusinessDateBetween(
                        creditFacilityId,
                        fromDate,
                        toDate
                ))
                .thenReturn(null);

        GetCreditInterestAccrualTotalResult result =
                handler.execute(query);

        assertEquals(BigDecimal.ZERO, result.totalInterestAmount());

        verify(repository)
                .sumInterestAmountByCreditFacilityIdAndBusinessDateBetween(
                        creditFacilityId,
                        fromDate,
                        toDate
                );
    }

    @Test
    void shouldThrowExceptionWhenQueryIsNull() {
        IllegalArgumentException exception =
                assertThrows(
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
    void shouldThrowExceptionWhenCreditFacilityIdIsNull() {
        GetCreditInterestAccrualTotalQuery query =
                new GetCreditInterestAccrualTotalQuery(
                        null,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                "creditFacilityId must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenFromDateIsNull() {
        GetCreditInterestAccrualTotalQuery query =
                new GetCreditInterestAccrualTotalQuery(
                        UUID.randomUUID(),
                        null,
                        LocalDate.of(2026, 9, 30)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                "fromDate must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenToDateIsNull() {
        GetCreditInterestAccrualTotalQuery query =
                new GetCreditInterestAccrualTotalQuery(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 1),
                        null
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                "toDate must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenFromDateIsAfterToDate() {
        GetCreditInterestAccrualTotalQuery query =
                new GetCreditInterestAccrualTotalQuery(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 30),
                        LocalDate.of(2026, 9, 1)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                "fromDate must not be after toDate",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }
}