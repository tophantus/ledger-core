package com.example.ledgercore.transaction.adapter.inbound.rest.dto;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Getter
public class TransactionFilterRequest {

    private TransactionStatus status;

    private TransactionType type;

    private String currency;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant to;

    private int page = 0;

    private int size = 20;
}