package com.example.ledgercore.businessday.event;

import java.time.LocalDate;

public record BusinessDayClosedEvent(
        LocalDate businessDate
) {
}