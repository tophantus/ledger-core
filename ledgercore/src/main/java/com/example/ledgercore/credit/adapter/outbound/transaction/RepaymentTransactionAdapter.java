package com.example.ledgercore.credit.adapter.outbound.transaction;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.RepaymentTransactionPort;
import com.example.ledgercore.transaction.command.dto.RepayCreditCommand;
import com.example.ledgercore.transaction.command.port.inbound.RepayCreditUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RepaymentTransactionAdapter
        implements RepaymentTransactionPort {

    private final RepayCreditUseCase
            repayCreditUseCase;

    @Override
    public void repay(
            UUID accountId,
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    ) {
        repayCreditUseCase.execute(
                new RepayCreditCommand(
                        accountId,
                        creditFacilityId,
                        amount,
                        currency,
                        reference,
                        description
                )
        );
    }
}