package com.example.ledgercore.credit.adapter.outbound.businessday;

import com.example.ledgercore.businessday.query.dto.BusinessDayResponse;
import com.example.ledgercore.businessday.query.port.inbound.GetCurrentBusinessDayUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditBusinessDatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CreditBusinessDateAdapter
        implements CreditBusinessDatePort {

    private final GetCurrentBusinessDayUseCase
            getCurrentBusinessDayUseCase;

    @Override
    public LocalDate getCurrentBusinessDate() {
        BusinessDayResponse response =
                getCurrentBusinessDayUseCase.execute();

        return response.businessDate();
    }
}