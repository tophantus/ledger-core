package com.example.ledgercore.cardtoken.adapter.inbound.rest.dto;

public record ProvisionCardTokenRequest(
        String pan,
        Short expiryMonth,
        Short expiryYear,
        String cvv,
        String providerCustomerReference
) {
}