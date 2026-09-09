package com.example.ledgercore.withdrawal.command.port.inbound;

import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.command.dto.WithdrawalRequestResponse;

public interface CreateWithdrawalRequestUseCase {

    WithdrawalRequestResponse execute(
            CreateWithdrawalRequestCommand command
    );
}