package com.example.ledgercore.card.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "card.security.encryption")
public class CardEncryptionProperties {

    private String activeVersion;

    private Map<String, String> keys;
}