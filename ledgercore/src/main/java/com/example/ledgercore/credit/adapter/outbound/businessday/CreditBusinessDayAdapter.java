package com.example.ledgercore.credit.adapter.outbound.businessday;

import com.example.ledgercore.businessday.query.dto.BusinessDayResponse;
import com.example.ledgercore.businessday.query.port.inbound.GetCurrentBusinessDayUseCase;
import com.example.ledgercore.credit.command.port.outbound.BusinessDateProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditBusinessDayAdapter implements BusinessDateProviderPort {

    private final GetCurrentBusinessDayUseCase getCurrentBusinessDayUseCase;

    @Override
    public java.time.LocalDate getCurrentBusinessDate() {
        BusinessDayResponse businessDay =
                getCurrentBusinessDayUseCase.execute();

        return businessDay.businessDate();
    }
}