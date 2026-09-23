package com.example.ledgercore.transaction.adapter.outbound.businessday;

import com.example.ledgercore.businessday.query.port.inbound.GetCurrentBusinessDayUseCase;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TransactionBusinessDayAdapter implements TransactionBusinessDayPort {

    private final GetCurrentBusinessDayUseCase getCurrentBusinessDayUseCase;

    @Override
    public LocalDate getCurrentBusinessDate() {
        return getCurrentBusinessDayUseCase.execute().businessDate();
    }
}