package com.example.ledgercore.account.adapter.outbound.ledger;

import com.example.ledgercore.account.command.port.outbound.AccountLedgerPort;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.ledger.command.dto.CreateLedgerAccountCommand;
import com.example.ledgercore.ledger.command.port.inbound.CreateLedgerAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountLedgerAdapter implements AccountLedgerPort {

    private final CreateLedgerAccountUseCase createLedgerAccountUseCase;

    @Override
    public UUID createLedgerAccount(
            String accountNo,
            Currency currency
    ) {
        return createLedgerAccountUseCase.execute(
                new CreateLedgerAccountCommand(
                        accountNo,
                        currency
                )
        );
    }
}