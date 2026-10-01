package com.example.ledgercore.account.query.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountInterestBalanceProjection {

    UUID getAccountId();

    BigDecimal getClosingBalance();
}