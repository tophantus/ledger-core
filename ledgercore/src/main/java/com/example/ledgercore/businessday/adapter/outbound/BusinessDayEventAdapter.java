package com.example.ledgercore.businessday.adapter.outbound;

import com.example.ledgercore.businessday.command.port.outbound.BusinessDayEventPort;
import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.outbox.event.OutboxAggregateType;
import com.example.ledgercore.outbox.event.OutboxEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BusinessDayEventAdapter
        implements BusinessDayEventPort {

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;

    @Override
    public void publishBusinessDayClosed(
            BusinessDayClosedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                OutboxAggregateType.BUSINESS_DAY.getValue(),
                businessDayAggregateId(event.businessDate()),
                OutboxEventType.BUSINESS_DAY_CLOSED.getValue(),
                event
        );
    }

    private UUID businessDayAggregateId(
            LocalDate businessDate
    ) {
        return UUID.nameUUIDFromBytes(
                businessDate.toString()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}