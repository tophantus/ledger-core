package com.example.ledgercore.withdrawal.adapter.outbound.atm;

import com.example.ledgercore.atm.command.dto.AtmAuthenticationResult;
import com.example.ledgercore.atm.command.dto.AuthenticateAtmCommand;
import com.example.ledgercore.atm.command.port.inbound.AuthenticateAtmUseCase;
import com.example.ledgercore.withdrawal.command.port.outbound.AtmAuthenticationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawalAtmAdapter
        implements AtmAuthenticationPort {

    private final AuthenticateAtmUseCase
            authenticateAtmUseCase;

    @Override
    public UUID authenticate(
            String terminalCode,
            String credential
    ) {
        AtmAuthenticationResult result =
                authenticateAtmUseCase.execute(
                        new AuthenticateAtmCommand(
                                terminalCode,
                                credential
                        )
                );

        return result.atmTerminalId();
    }
}