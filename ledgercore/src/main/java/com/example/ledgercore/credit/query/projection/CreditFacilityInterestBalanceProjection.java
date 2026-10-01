package com.example.ledgercore.credit.query.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditFacilityInterestBalanceProjection {

    UUID getCreditFacilityId();

    BigDecimal getClosingBalance();
}