package com.example.ledgercore.card.command.port.inbound;

import com.example.ledgercore.card.command.dto.VerifyCardCredentialsCommand;
import com.example.ledgercore.card.command.dto.VerifyCardCredentialsResult;

public interface VerifyCardCredentialsUseCase {

    VerifyCardCredentialsResult execute(
            VerifyCardCredentialsCommand command
    );
}