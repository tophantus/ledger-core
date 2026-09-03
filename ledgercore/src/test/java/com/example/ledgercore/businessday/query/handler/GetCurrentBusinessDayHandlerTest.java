package com.example.ledgercore.businessday.query.handler;

import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import com.example.ledgercore.businessday.query.dto.BusinessDayResponse;
import com.example.ledgercore.businessday.query.repository.BusinessDayQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentBusinessDayHandlerTest {

    @Mock
    private BusinessDayQueryRepository repository;

    private GetCurrentBusinessDayHandler handler;

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 8, 27);

    private static final Instant OPENED_AT =
            Instant.parse("2026-08-26T16:00:00Z");

    @BeforeEach
    void setUp() {
        handler = new GetCurrentBusinessDayHandler(
                repository
        );
    }

    @Test
    void shouldReturnCurrentOpenBusinessDay() {

        BusinessDay businessDay =
                BusinessDay.builder()
                        .businessDate(BUSINESS_DATE)
                        .status(BusinessDayStatus.OPEN)
                        .openedAt(OPENED_AT)
                        .build();

        when(repository.findByStatus(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        BusinessDayResponse response =
                handler.execute();

        assertNotNull(response);

        assertEquals(
                BUSINESS_DATE,
                response.businessDate()
        );

        assertEquals(
                BusinessDayStatus.OPEN,
                response.status()
        );

        assertEquals(
                OPENED_AT,
                response.openedAt()
        );

        assertNull(
                response.closedAt()
        );

        verify(repository)
                .findByStatus(
                        BusinessDayStatus.OPEN
                );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowWhenNoOpenBusinessDayExists() {

        when(repository.findByStatus(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.BUSINESS_DAY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(repository)
                .findByStatus(
                        BusinessDayStatus.OPEN
                );

        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldReturnExactlyMappedBusinessDayData() {

        Instant closedAt =
                Instant.parse("2026-08-27T16:30:00Z");

        BusinessDay businessDay =
                BusinessDay.builder()
                        .businessDate(BUSINESS_DATE)
                        .status(BusinessDayStatus.CLOSED)
                        .openedAt(OPENED_AT)
                        .closedAt(closedAt)
                        .build();

        when(repository.findByStatus(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        BusinessDayResponse response =
                handler.execute();

        assertEquals(
                BUSINESS_DATE,
                response.businessDate()
        );

        assertEquals(
                BusinessDayStatus.CLOSED,
                response.status()
        );

        assertEquals(
                OPENED_AT,
                response.openedAt()
        );

        assertEquals(
                closedAt,
                response.closedAt()
        );

        verify(repository)
                .findByStatus(
                        BusinessDayStatus.OPEN
                );

        verifyNoMoreInteractions(repository);
    }
}