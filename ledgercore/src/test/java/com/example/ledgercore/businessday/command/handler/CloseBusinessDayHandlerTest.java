package com.example.ledgercore.businessday.command.handler;

import com.example.ledgercore.businessday.command.port.outbound.BusinessDayEventPort;
import com.example.ledgercore.businessday.command.repository.BusinessDayCommandRepository;
import com.example.ledgercore.businessday.config.BusinessDayProperties;
import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloseBusinessDayHandlerTest {

    @Mock
    private BusinessDayCommandRepository businessDayCommandRepository;

    @Mock
    private BusinessDayProperties businessDayProperties;

    @Mock
    private BusinessDayEventPort businessDayEventPort;

    @Mock
    private Clock clock;

    @Mock
    private BusinessDay businessDay;

    @InjectMocks
    private CloseBusinessDayHandler handler;

    @Test
    void execute_shouldCloseBusinessDayAndOpenNextDay() {

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant now = Instant.parse("2026-09-04T16:40:00Z");

        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(true);

        when(businessDayProperties.getClosingStart())
                .thenReturn(LocalTime.of(23, 30));

        when(businessDayCommandRepository.existsById(
                businessDate.plusDays(1)
        )).thenReturn(false);

        handler.execute();

        verify(businessDay).close(now);

        ArgumentCaptor<BusinessDay> captor =
                ArgumentCaptor.forClass(BusinessDay.class);

        verify(businessDayCommandRepository)
                .save(captor.capture());

        BusinessDay nextBusinessDay = captor.getValue();

        assertEquals(
                businessDate.plusDays(1),
                nextBusinessDay.getBusinessDate()
        );

        assertEquals(
                BusinessDayStatus.OPEN,
                nextBusinessDay.getStatus()
        );

        assertEquals(
                now,
                nextBusinessDay.getOpenedAt()
        );

        ArgumentCaptor<BusinessDayClosedEvent> eventCaptor =
                ArgumentCaptor.forClass(BusinessDayClosedEvent.class);

        verify(businessDayEventPort)
                .publishBusinessDayClosed(eventCaptor.capture());

        assertEquals(
                businessDate,
                eventCaptor.getValue().businessDate()
        );
    }

    @Test
    void execute_shouldThrow_whenBusinessDayNotFound() {

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(Instant.parse("2026-09-04T16:40:00Z"));

        when(businessDayCommandRepository.findByStatusForUpdate(
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

        verify(businessDayCommandRepository, never())
                .save(any());

        verify(businessDayEventPort, never())
                .publishBusinessDayClosed(any());
    }

    @Test
    void execute_shouldThrow_whenClosingTimeHasNotStarted() {

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        // 2026-09-04 22:40 Vietnam time
        Instant now = Instant.parse("2026-09-04T15:40:00Z");

        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        when(businessDayProperties.getTimezone())
                .thenReturn(zoneId.getId());

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(true);

        when(businessDayProperties.getClosingStart())
                .thenReturn(LocalTime.of(23, 30));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.BUSINESS_DAY_CLOSE_NOT_ALLOWED,
                exception.getErrorCode()
        );

        verify(businessDay, never())
                .close(any());

        verify(businessDayCommandRepository, never())
                .existsById(any());

        verify(businessDayCommandRepository, never())
                .save(any());

        verify(businessDayEventPort, never())
                .publishBusinessDayClosed(any());
    }

    @Test
    void execute_shouldClose_whenCurrentTimeEqualsClosingStart() {

        // 2026-09-04 23:30 Vietnam time
        Instant now = Instant.parse("2026-09-04T16:30:00Z");

        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(true);

        when(businessDayProperties.getClosingStart())
                .thenReturn(LocalTime.of(23, 30));

        when(businessDayCommandRepository.existsById(
                businessDate.plusDays(1)
        )).thenReturn(false);

        assertDoesNotThrow(() -> handler.execute());

        verify(businessDay).close(now);

        verify(businessDayCommandRepository)
                .save(any(BusinessDay.class));

        verify(businessDayEventPort)
                .publishBusinessDayClosed(any(BusinessDayClosedEvent.class));
    }

    @Test
    void execute_shouldClose_whenBusinessDateIsYesterday() {

        // 2026-09-05 10:00 Vietnam time
        Instant now = Instant.parse("2026-09-05T03:00:00Z");

        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(true);

        when(businessDayProperties.getClosingStart())
                .thenReturn(LocalTime.of(23, 30));

        when(businessDayCommandRepository.existsById(
                businessDate.plusDays(1)
        )).thenReturn(false);

        assertDoesNotThrow(() -> handler.execute());

        verify(businessDay).close(now);

        verify(businessDayCommandRepository)
                .save(any(BusinessDay.class));

        verify(businessDayEventPort)
                .publishBusinessDayClosed(any());
    }

    @Test
    void execute_shouldThrow_whenBusinessDateIsInFuture() {

        // Current date: Sep 4
        Instant now = Instant.parse("2026-09-04T16:40:00Z");

        // Business day: Sep 5
        LocalDate businessDate = LocalDate.of(2026, 9, 5);

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(true);

        when(businessDayProperties.getClosingStart())
                .thenReturn(LocalTime.of(23, 30));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.BUSINESS_DAY_DATE_MISMATCH,
                exception.getErrorCode()
        );

        verify(businessDay, never())
                .close(any());

        verify(businessDayCommandRepository, never())
                .existsById(any());

        verify(businessDayCommandRepository, never())
                .save(any());

        verify(businessDayEventPort, never())
                .publishBusinessDayClosed(any());
    }

    @Test
    void execute_shouldThrow_whenNextBusinessDayAlreadyExists() {

        Instant now = Instant.parse("2026-09-04T16:40:00Z");

        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(true);

        when(businessDayProperties.getClosingStart())
                .thenReturn(LocalTime.of(23, 30));

        when(businessDayCommandRepository.existsById(
                businessDate.plusDays(1)
        )).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.NEXT_BUSINESS_DAY_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        /*
         * close() happens before openNextBusinessDay().
         * The transaction will roll back if this is a real Spring transaction.
         */
        verify(businessDay).close(now);

        verify(businessDayCommandRepository, never())
                .save(any());

        verify(businessDayEventPort, never())
                .publishBusinessDayClosed(any());
    }

    @Test
    void execute_shouldSkipClosingTimeValidation_whenValidationDisabled() {

        // 2026-09-04 10:00 Vietnam time
        Instant now = Instant.parse("2026-09-04T03:00:00Z");

        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        when(businessDayProperties.getTimezone())
                .thenReturn("Asia/Ho_Chi_Minh");

        when(clock.instant())
                .thenReturn(now);

        when(businessDayCommandRepository.findByStatusForUpdate(
                BusinessDayStatus.OPEN
        )).thenReturn(Optional.of(businessDay));

        when(businessDay.getBusinessDate())
                .thenReturn(businessDate);

        when(businessDayProperties.isClosingValidationEnabled())
                .thenReturn(false);

        when(businessDayCommandRepository.existsById(
                businessDate.plusDays(1)
        )).thenReturn(false);

        assertDoesNotThrow(() -> handler.execute());

        verify(businessDay).close(now);

        verify(businessDayCommandRepository)
                .save(any(BusinessDay.class));

        verify(businessDayEventPort)
                .publishBusinessDayClosed(any());
    }
}