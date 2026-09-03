package com.example.ledgercore.businessday.query.handler;

import com.example.ledgercore.businessday.entity.BusinessDay;
import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import com.example.ledgercore.businessday.query.dto.BusinessDayResponse;
import com.example.ledgercore.businessday.query.port.inbound.GetCurrentBusinessDayUseCase;
import com.example.ledgercore.businessday.query.repository.BusinessDayQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentBusinessDayHandler
        implements GetCurrentBusinessDayUseCase {

    private final BusinessDayQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public BusinessDayResponse execute() {

        BusinessDay businessDay =
                repository.findByStatus(BusinessDayStatus.OPEN)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BUSINESS_DAY_NOT_FOUND
                                )
                        );

        return new BusinessDayResponse(
                businessDay.getBusinessDate(),
                businessDay.getStatus(),
                businessDay.getOpenedAt(),
                businessDay.getClosedAt()
        );
    }
}