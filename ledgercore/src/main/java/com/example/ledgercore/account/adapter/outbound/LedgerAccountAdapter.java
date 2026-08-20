package com.example.ledgercore.account.adapter.outbound;

import com.example.ledgercore.account.command.port.outbound.LedgerAccountPort;
import com.example.ledgercore.ledger.command.dto.CreateLedgerAccountCommand;
import com.example.ledgercore.ledger.command.port.inbound.CreateLedgerAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerAccountAdapter implements LedgerAccountPort {

    private final CreateLedgerAccountUseCase createLedgerAccountUseCase;

    @Override
    public UUID createCustomerAccount(
            String accountNo,
            String currency
    ) {
        return createLedgerAccountUseCase.execute(
                new CreateLedgerAccountCommand(
                        accountNo,
                        currency
                )
        );
    }
}