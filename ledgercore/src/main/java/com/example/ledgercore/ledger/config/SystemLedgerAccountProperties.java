package com.example.ledgercore.ledger.config;

import com.example.ledgercore.common.currency.Currency;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "ledger.system-account")
public class SystemLedgerAccountProperties {

    private Map<Currency, String> cashCodes;

    private Map<Currency, String> interestExpenseCodes;

    private Map<Currency, String> interestPayableCodes;
}