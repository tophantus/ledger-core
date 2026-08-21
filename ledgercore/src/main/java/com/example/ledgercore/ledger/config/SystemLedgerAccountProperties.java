package com.example.ledgercore.ledger.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "ledger.system-account")
public class SystemLedgerAccountProperties {

    private Map<String, String> cashCodes;
}