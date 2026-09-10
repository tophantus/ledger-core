package com.example.ledgercore.withdrawal.command.port.inbound;

import com.example.ledgercore.withdrawal.command.dto.CancelWithdrawalIntentCommand;

public interface CancelWithdrawalIntentUseCase {

    void execute(
            CancelWithdrawalIntentCommand command
    );
}