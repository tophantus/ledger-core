package com.example.ledgercore.cardtoken.command.port.inbound;

import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenCommand;
import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenResult;

public interface ProvisionCardTokenUseCase {

    ProvisionCardTokenResult execute(
            ProvisionCardTokenCommand command
    );
}