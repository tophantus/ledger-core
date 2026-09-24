package com.example.ledgercore.withdrawal.command.port.outbound;

import com.example.ledgercore.withdrawal.command.port.outbound.dto.WithdrawalAccountInfo;

import java.util.UUID;

public interface WithdrawalAccountPort {

    WithdrawalAccountInfo getWithdrawalInfo(
            UUID accountId
    );
}
