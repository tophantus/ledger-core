package com.example.ledgercore.card.adapter.outbound.transaction;

import com.example.ledgercore.card.command.port.outbound.CardProviderTransferPort;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.transaction.command.dto.TransferCreditCardToProviderCommand;
import com.example.ledgercore.transaction.command.dto.TransferDebitCardToProviderCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferCreditCardToProviderUseCase;
import com.example.ledgercore.transaction.command.port.inbound.TransferDebitCardToProviderUseCase;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardProviderTransferAdapter
        implements CardProviderTransferPort {

    private final TransferDebitCardToProviderUseCase
            transferDebitCardToProviderUseCase;

    private final TransferCreditCardToProviderUseCase
            transferCreditCardToProviderUseCase;

    @Override
    public UUID transferFromDebitCard(
            UUID sourceAccountId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    ) {
        TransactionResponse response =
                transferDebitCardToProviderUseCase.execute(
                        new TransferDebitCardToProviderCommand(
                                sourceAccountId,
                                providerAccountId,
                                amount,
                                currency,
                                reference,
                                description
                        )
                );

        return response.id();
    }

    @Override
    public UUID transferFromCreditCard(
            UUID creditFacilityId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    ) {
        TransactionResponse response =
                transferCreditCardToProviderUseCase.execute(
                        new TransferCreditCardToProviderCommand(
                                creditFacilityId,
                                providerAccountId,
                                amount,
                                currency,
                                reference,
                                description
                        )
                );

        return response.id();
    }
}
