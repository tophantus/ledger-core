package com.example.ledgercore.withdrawal.command.port.inbound;

import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.query.dto.ConfirmWithdrawalRequestResponse;

public interface ConfirmWithdrawalRequestUseCase {

    ConfirmWithdrawalRequestResponse execute(
            ConfirmWithdrawalRequestCommand command
    );
}