package com.example.ledgercore.account.port.outbound;

import java.util.UUID;

public interface ProductAccountPort {

    ProductAccountInfo getActiveProduct(UUID productId);
}