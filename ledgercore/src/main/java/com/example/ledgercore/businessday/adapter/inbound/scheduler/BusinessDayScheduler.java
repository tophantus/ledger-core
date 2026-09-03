package com.example.ledgercore.businessday.adapter.inbound.scheduler;

import com.example.ledgercore.businessday.command.port.inbound.CloseBusinessDayUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessDayScheduler {

    private final CloseBusinessDayUseCase closeBusinessDayUseCase;

    @Scheduled(
            cron = "${business-day.closing-cron}",
            zone = "${business-day.timezone}"
    )
    public void closeBusinessDay() {
        closeBusinessDayUseCase.execute();
    }
}