package com.example.ledgercore.transaction.adapter.inbound.rest.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class TransactionFilterRequest {

    private UUID accountId;

    private TransactionStatus status;

    private TransactionType type;

    private Currency currency;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant to;

    private int page = 0;

    private int size = 20;
}