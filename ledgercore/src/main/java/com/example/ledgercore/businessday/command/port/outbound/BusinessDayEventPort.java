package com.example.ledgercore.businessday.command.port.outbound;

import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;

public interface BusinessDayEventPort {

    void publishBusinessDayClosed(
            BusinessDayClosedEvent event
    );
}