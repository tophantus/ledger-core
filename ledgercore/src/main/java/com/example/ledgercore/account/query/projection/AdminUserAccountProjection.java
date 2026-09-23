package com.example.ledgercore.account.query.projection;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface AdminUserAccountProjection {

    UUID getAccountId();

    UUID getUserId();

    UUID getProductId();

    String getAccountNo();

    Currency getCurrency();

    BigDecimal getBalance();

    BigDecimal getHoldAmount();

    AccountStatus getStatus();

    Long getVersion();

    UUID getLedgerAccountId();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}