package com.example.ledgercore.interest.adapter.outbound.transaction;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.port.outbound.InterestTransactionPort;
import com.example.ledgercore.transaction.command.dto.PostInterestTransactionCommand;
import com.example.ledgercore.transaction.command.port.inbound.PostInterestTransactionUseCase;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InterestTransactionAdapter
        implements InterestTransactionPort {

    private final PostInterestTransactionUseCase useCase;

    @Override
    public UUID postInterest(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        TransactionResponse response =
                useCase.execute(
                        new PostInterestTransactionCommand(
                                accountId,
                                amount,
                                currency,
                                businessDate
                        )
                );

        return response.id();
    }
}