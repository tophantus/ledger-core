package com.example.ledgercore.ledger.adapter.outbound.credit;

import com.example.ledgercore.credit.query.port.inbound.GetCreditFacilityLedgerAccountUseCase;
import com.example.ledgercore.ledger.command.port.outbound.CreditFacilityLedgerMappingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditFacilityLedgerMappingAdapter
        implements CreditFacilityLedgerMappingPort {

    private final GetCreditFacilityLedgerAccountUseCase
            getCreditFacilityLedgerAccountUseCase;

    @Override
    public UUID getLedgerAccountId(UUID creditFacilityId) {
        return getCreditFacilityLedgerAccountUseCase.execute(
                creditFacilityId
        );
    }
}