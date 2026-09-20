package com.example.ledgercore.account.port.outbound;

import com.example.ledgercore.account.port.outbound.dto.ProductAccountInfo;

import java.util.UUID;

public interface ProductAccountPort {

    ProductAccountInfo getActiveProduct(UUID productId);
}