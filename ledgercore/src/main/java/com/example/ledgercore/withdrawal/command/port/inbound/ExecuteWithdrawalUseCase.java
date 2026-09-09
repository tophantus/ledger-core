package com.example.ledgercore.withdrawal.command.port.inbound;

import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalCommand;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalResponse;

public interface ExecuteWithdrawalUseCase {

    ExecuteWithdrawalResponse execute(
            ExecuteWithdrawalCommand command
    );
}