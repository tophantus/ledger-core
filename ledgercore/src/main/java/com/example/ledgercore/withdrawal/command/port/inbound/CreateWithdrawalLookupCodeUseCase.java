package com.example.ledgercore.withdrawal.command.port.inbound;

import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeCommand;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeResponse;

public interface CreateWithdrawalLookupCodeUseCase {

    CreateWithdrawalLookupCodeResponse execute(
            CreateWithdrawalLookupCodeCommand command
    );
}