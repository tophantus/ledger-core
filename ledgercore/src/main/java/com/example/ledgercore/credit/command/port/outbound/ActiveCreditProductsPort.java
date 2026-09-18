package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;

import java.util.List;

public interface ActiveCreditProductsPort {

    List<ActiveCreditProductInfo> getActiveCreditProducts();
}