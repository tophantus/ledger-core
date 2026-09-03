package com.example.ledgercore.businessday.query.port.inbound;

import com.example.ledgercore.businessday.query.dto.BusinessDayResponse;

public interface GetCurrentBusinessDayUseCase {

    BusinessDayResponse execute();
}