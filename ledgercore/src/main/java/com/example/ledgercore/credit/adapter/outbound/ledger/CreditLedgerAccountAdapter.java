package com.example.ledgercore.credit.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.CreditLedgerAccountPort;
import com.example.ledgercore.ledger.command.dto.CreateCreditLedgerAccountCommand;
import com.example.ledgercore.ledger.command.port.inbound.CreateCreditLedgerAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditLedgerAccountAdapter
        implements CreditLedgerAccountPort {

    private final CreateCreditLedgerAccountUseCase
            createCreditLedgerAccountUseCase;

    @Override
    public UUID createCreditLedgerAccount(
            UUID creditFacilityId,
            Currency currency
    ) {
        return createCreditLedgerAccountUseCase.execute(
                new CreateCreditLedgerAccountCommand(
                        creditFacilityId,
                        currency
                )
        );
    }
}