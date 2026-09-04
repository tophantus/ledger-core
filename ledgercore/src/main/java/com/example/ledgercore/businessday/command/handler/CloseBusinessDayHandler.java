package com.example.ledgercore.businessday.command.handler;

import com.example.ledgercore.businessday.command.port.inbound.CloseBusinessDayUseCase;
import com.example.ledgercore.businessday.command.port.outbound.BusinessDayEventPort;
import com.example.ledgercore.businessday.command.repository.BusinessDayCommandRepository;
import com.example.ledgercore.businessday.config.BusinessDayProperties;
import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CloseBusinessDayHandler
        implements CloseBusinessDayUseCase {

    private final BusinessDayCommandRepository
            businessDayCommandRepository;

    private final BusinessDayProperties
            businessDayProperties;

    private final BusinessDayEventPort businessDayEventPort;

    private final Clock clock;

    @Override
    @Transactional
    public void execute() {

        ZoneId zoneId =
                ZoneId.of(
                        businessDayProperties.getTimezone()
                );

        Instant now = Instant.now(clock);

        LocalDate currentDate =
                LocalDate.ofInstant(now, zoneId);

        LocalTime currentTime =
                LocalTime.ofInstant(now, zoneId);

        BusinessDay businessDay =
                businessDayCommandRepository
                        .findByStatusForUpdate(
                                BusinessDayStatus.OPEN
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BUSINESS_DAY_NOT_FOUND
                                )
                        );

        LocalDate businessDate =
                businessDay.getBusinessDate();

        validateClosingTime(
                businessDate,
                currentDate,
                currentTime
        );

        businessDay.close(now);

        openNextBusinessDay(
                businessDate.plusDays(1),
                now
        );

        businessDayEventPort.publishBusinessDayClosed(
                new BusinessDayClosedEvent(
                        businessDate
                )
        );
    }

    private void validateClosingTime(
            LocalDate businessDate,
            LocalDate currentDate,
            LocalTime currentTime
    ) {

        LocalTime closingStart =
                businessDayProperties.getClosingStart();

        /*
         * Normal case:
         *
         * businessDate = currentDate
         * currentTime >= 23:30
         *
         * Example:
         *  Sep 4 23:40
         */
        if (businessDate.equals(currentDate)) {

            if (currentTime.isBefore(closingStart)) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_DAY_CLOSE_NOT_ALLOWED
                );
            }

            return;
        }

        /*
         * Recovery case:
         *
         * Current date has already passed the business date.
         *
         * Example:
         *  businessDate = Sep 4
         *  currentDate  = Sep 5
         *
         * This means yesterday's EOD was missed.
         * Allow closing it.
         */
        if (businessDate.isBefore(currentDate)) {
            return;
        }

        /*
         * businessDate is in the future.
         */
        throw new BusinessException(
                ErrorCode.BUSINESS_DAY_DATE_MISMATCH
        );
    }

    private void openNextBusinessDay(
            LocalDate businessDate,
            Instant now
    ) {

        if (businessDayCommandRepository
                .existsById(businessDate)) {

            throw new BusinessException(
                    ErrorCode.NEXT_BUSINESS_DAY_ALREADY_EXISTS
            );
        }

        BusinessDay nextBusinessDay =
                BusinessDay.builder()
                        .businessDate(businessDate)
                        .status(BusinessDayStatus.OPEN)
                        .openedAt(now)
                        .build();

        businessDayCommandRepository.save(
                nextBusinessDay
        );
    }
}