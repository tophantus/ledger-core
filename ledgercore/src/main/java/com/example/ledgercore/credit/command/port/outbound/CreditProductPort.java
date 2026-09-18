package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.command.port.outbound.dto.CreditProductInfo;

import java.util.UUID;

public interface CreditProductPort {

    CreditProductInfo getActiveProduct(UUID productId);
}