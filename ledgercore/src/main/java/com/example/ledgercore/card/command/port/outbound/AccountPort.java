package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.card.command.port.outbound.dto.AccountInfo;

import java.util.UUID;

public interface AccountPort {

    AccountInfo getOwnedAccount(
            UUID customerId,
            UUID accountId
    );
}