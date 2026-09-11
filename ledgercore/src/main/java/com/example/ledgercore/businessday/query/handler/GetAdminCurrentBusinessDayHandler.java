package com.example.ledgercore.businessday.query.handler;

import com.example.ledgercore.businessday.config.BusinessDayProperties;
import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import com.example.ledgercore.businessday.query.dto.CurrentBusinessDayResponse;
import com.example.ledgercore.businessday.query.port.inbound.GetAdminCurrentBusinessDayUseCase;
import com.example.ledgercore.businessday.query.repository.BusinessDayQueryRepository;
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
public class GetAdminCurrentBusinessDayHandler
        implements GetAdminCurrentBusinessDayUseCase {

    private final BusinessDayQueryRepository
            businessDayQueryRepository;

    private final BusinessDayProperties
            businessDayProperties;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public CurrentBusinessDayResponse execute() {

        BusinessDay businessDay =
                businessDayQueryRepository
                        .findByStatus(BusinessDayStatus.OPEN)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BUSINESS_DAY_NOT_FOUND
                                )
                        );

        ZoneId zoneId =
                ZoneId.of(
                        businessDayProperties.getTimezone()
                );

        Instant now = Instant.now(clock);

        LocalDate currentDate =
                LocalDate.ofInstant(
                        now,
                        zoneId
                );

        LocalTime currentTime =
                LocalTime.ofInstant(
                        now,
                        zoneId
                );

        boolean canClose =
                canClose(
                        businessDay.getBusinessDate(),
                        currentDate,
                        currentTime
                );

        return new CurrentBusinessDayResponse(
                businessDay.getBusinessDate(),
                businessDay.getStatus(),
                canClose
        );
    }

    private boolean canClose(
            LocalDate businessDate,
            LocalDate currentDate,
            LocalTime currentTime
    ) {
        if (businessDate.isBefore(currentDate)) {
            return true;
        }

        if (businessDate.isAfter(currentDate)) {
            return false;
        }

        return !currentTime.isBefore(
                businessDayProperties.getClosingStart()
        );
    }
}