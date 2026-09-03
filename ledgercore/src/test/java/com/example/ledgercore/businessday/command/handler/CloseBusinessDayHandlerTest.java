package com.example.ledgercore.businessday.command.handler;

import com.example.ledgercore.businessday.command.repository.BusinessDayCommandRepository;
import com.example.ledgercore.businessday.config.BusinessDayProperties;
import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private static final ZoneId ZONE_ID =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final LocalTime CLOSING_START =
            LocalTime.of(23, 30);

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 8, 27);

    /*
     * 23:30 Asia/Ho_Chi_Minh
     */
    private static final Instant CLOSING_TIME =
            Instant.parse("2026-08-27T16:30:00Z");

    /*
     * 23:29 Asia/Ho_Chi_Minh
     */
    private static final Instant BEFORE_CLOSING_TIME =
            Instant.parse("2026-08-27T16:29:00Z");

    /*
     * 08:00 Asia/Ho_Chi_Minh on next day.
     */
    private static final Instant RECOVERY_TIME =
            Instant.parse("2026-08-28T01:00:00Z");

    @Mock
    private BusinessDayCommandRepository
            businessDayCommandRepository;

    @Mock
    private BusinessDayProperties
            businessDayProperties;

    private CloseBusinessDayHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CloseBusinessDayHandler(
                businessDayCommandRepository,
                businessDayProperties,
                Clock.fixed(
                        CLOSING_TIME,
                        ZONE_ID
                )
        );
    }

    @Test
    void shouldCloseBusinessDayAndOpenNextBusinessDay() {

        BusinessDay businessDay =
                openBusinessDay(BUSINESS_DATE);

        stubTimezone();

        stubClosingStart();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.of(businessDay));

        when(businessDayCommandRepository
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                ))
                .thenReturn(false);

        handler.execute();

        assertEquals(
                BusinessDayStatus.CLOSED,
                businessDay.getStatus()
        );

        assertEquals(
                CLOSING_TIME,
                businessDay.getClosedAt()
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verify(businessDayCommandRepository)
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                );

        verify(businessDayCommandRepository)
                .save(argThat(nextBusinessDay ->
                        nextBusinessDay.getBusinessDate()
                                .equals(BUSINESS_DATE.plusDays(1))
                                && nextBusinessDay.getStatus()
                                == BusinessDayStatus.OPEN
                                && nextBusinessDay.getOpenedAt()
                                .equals(CLOSING_TIME)
                                && nextBusinessDay.getClosedAt()
                                == null
                ));

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    @Test
    void shouldAllowCloseExactlyAtClosingStart() {

        BusinessDay businessDay =
                openBusinessDay(BUSINESS_DATE);

        stubTimezone();

        stubClosingStart();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.of(businessDay));

        when(businessDayCommandRepository
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                ))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> handler.execute()
        );

        assertEquals(
                BusinessDayStatus.CLOSED,
                businessDay.getStatus()
        );

        assertEquals(
                CLOSING_TIME,
                businessDay.getClosedAt()
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verify(businessDayCommandRepository)
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                );

        verify(businessDayCommandRepository)
                .save(any(BusinessDay.class));

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    @Test
    void shouldRejectCloseBeforeClosingStart() {

        handler = new CloseBusinessDayHandler(
                businessDayCommandRepository,
                businessDayProperties,
                Clock.fixed(
                        BEFORE_CLOSING_TIME,
                        ZONE_ID
                )
        );

        BusinessDay businessDay =
                openBusinessDay(BUSINESS_DATE);

        stubTimezone();

        stubClosingStart();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.of(businessDay));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.BUSINESS_DAY_CLOSE_NOT_ALLOWED,
                exception.getErrorCode()
        );

        assertEquals(
                BusinessDayStatus.OPEN,
                businessDay.getStatus()
        );

        assertNull(
                businessDay.getClosedAt()
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    @Test
    void shouldCloseMissedBusinessDayDuringRecovery() {

        handler = new CloseBusinessDayHandler(
                businessDayCommandRepository,
                businessDayProperties,
                Clock.fixed(
                        RECOVERY_TIME,
                        ZONE_ID
                )
        );

        BusinessDay businessDay =
                openBusinessDay(BUSINESS_DATE);

        stubTimezone();

        /*
         * ClosingStart is still required by the
         * current implementation because validateClosingTime()
         * reads it before checking the date.
         */
        stubClosingStart();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.of(businessDay));

        when(businessDayCommandRepository
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                ))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> handler.execute()
        );

        assertEquals(
                BusinessDayStatus.CLOSED,
                businessDay.getStatus()
        );

        assertEquals(
                RECOVERY_TIME,
                businessDay.getClosedAt()
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verify(businessDayCommandRepository)
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                );

        verify(businessDayCommandRepository)
                .save(argThat(nextBusinessDay ->
                        nextBusinessDay.getBusinessDate()
                                .equals(BUSINESS_DATE.plusDays(1))
                                && nextBusinessDay.getStatus()
                                == BusinessDayStatus.OPEN
                                && nextBusinessDay.getOpenedAt()
                                .equals(RECOVERY_TIME)
                ));

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    @Test
    void shouldThrowWhenNoOpenBusinessDayExists() {

        stubTimezone();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.BUSINESS_DAY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(businessDayProperties)
                .getTimezone();

        verifyNoMoreInteractions(
                businessDayProperties
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    @Test
    void shouldRejectFutureBusinessDay() {

        LocalDate futureBusinessDate =
                BUSINESS_DATE.plusDays(1);

        BusinessDay businessDay =
                openBusinessDay(futureBusinessDate);

        stubTimezone();

        stubClosingStart();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.of(businessDay));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute()
                );

        assertEquals(
                ErrorCode.BUSINESS_DAY_DATE_MISMATCH,
                exception.getErrorCode()
        );

        assertEquals(
                BusinessDayStatus.OPEN,
                businessDay.getStatus()
        );

        assertNull(
                businessDay.getClosedAt()
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    @Test
    void shouldRejectWhenNextBusinessDayAlreadyExists() {

        BusinessDay businessDay =
                openBusinessDay(BUSINESS_DATE);

        stubTimezone();

        stubClosingStart();

        when(businessDayCommandRepository
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                ))
                .thenReturn(Optional.of(businessDay));

        when(businessDayCommandRepository
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                ))
                .thenReturn(true);

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
         * The entity is mutated before the exception.
         *
         * In production, @Transactional rollback will
         * prevent this state from being committed.
         */
        assertEquals(
                BusinessDayStatus.CLOSED,
                businessDay.getStatus()
        );

        assertEquals(
                CLOSING_TIME,
                businessDay.getClosedAt()
        );

        verify(businessDayCommandRepository)
                .findByStatusForUpdate(
                        BusinessDayStatus.OPEN
                );

        verify(businessDayCommandRepository)
                .existsById(
                        BUSINESS_DATE.plusDays(1)
                );

        verifyNoMoreInteractions(
                businessDayCommandRepository
        );
    }

    private void stubTimezone() {
        when(businessDayProperties.getTimezone())
                .thenReturn(ZONE_ID.getId());
    }

    private void stubClosingStart() {
        when(businessDayProperties.getClosingStart())
                .thenReturn(CLOSING_START);
    }

    private BusinessDay openBusinessDay(
            LocalDate businessDate
    ) {
        return BusinessDay.builder()
                .businessDate(businessDate)
                .status(BusinessDayStatus.OPEN)
                .openedAt(
                        Instant.parse(
                                "2026-08-26T16:00:00Z"
                        )
                )
                .build();
    }
}