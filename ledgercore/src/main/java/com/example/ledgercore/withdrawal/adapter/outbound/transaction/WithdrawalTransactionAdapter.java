package com.example.ledgercore.withdrawal.adapter.outbound.transaction;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.WithdrawMoneyUseCase;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalTransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawalTransactionAdapter
        implements WithdrawalTransactionPort {

    private final WithdrawMoneyUseCase
            withdrawMoneyUseCase;

    @Override
    public UUID withdraw(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    ) {
        TransactionResponse response =
                withdrawMoneyUseCase.execute(
                        new WithdrawMoneyCommand(
                                accountId,
                                amount,
                                currency,
                                reference,
                                description
                        )
                );

        return response.id();
    }
}