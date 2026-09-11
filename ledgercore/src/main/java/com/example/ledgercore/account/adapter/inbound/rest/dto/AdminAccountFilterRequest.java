package com.example.ledgercore.account.adapter.inbound.rest.dto;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AdminAccountFilterRequest {

    private String accountNo;

    private AccountStatus status;

    private Currency currency;

    private UUID userId;

    private int page = 0;

    private int size = 20;
}