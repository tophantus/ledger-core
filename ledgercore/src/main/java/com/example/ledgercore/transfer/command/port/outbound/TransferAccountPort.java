package com.example.ledgercore.transfer.command.port.outbound;


import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferAccountPort {

    TransferAccountInfo getTransferInfo(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    );

    UUID getAccountIdByAccountNo(
            String accountNo
    );

    record TransferAccountInfo(
            UUID sourceAccountId,
            UUID destinationAccountId,
            Currency currency,
            BigDecimal sourceAvailableBalance
    ) {
    }
}
