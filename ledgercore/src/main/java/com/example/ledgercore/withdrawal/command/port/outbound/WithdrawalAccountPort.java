package com.example.ledgercore.withdrawal.command.port.outbound;

import java.util.UUID;

public interface WithdrawalAccountPort {

    WithdrawalAccountInfo getWithdrawalInfo(
            UUID accountId
    );
}
