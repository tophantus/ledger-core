package com.example.ledgercore.businessday.query.port.inbound;

import com.example.ledgercore.businessday.query.dto.CurrentBusinessDayResponse;

public interface GetAdminCurrentBusinessDayUseCase {
    CurrentBusinessDayResponse execute();
}
