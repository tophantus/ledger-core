package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;

import java.util.UUID;

public interface ActiveCreditProductPort {

    ActiveCreditProductInfo getActiveProduct(UUID productId);
}