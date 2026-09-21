package com.example.ledgercore.card.adapter.outbound.hold;

import com.example.ledgercore.card.command.port.outbound.AccountAuthorizationHoldPort;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldResponse;
import com.example.ledgercore.hold.command.port.inbound.CreateAccountHoldUseCase;
import com.example.ledgercore.hold.enums.AccountHoldReferenceType;
import com.example.ledgercore.hold.enums.AccountHoldType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountAuthorizationHoldAdapter
        implements AccountAuthorizationHoldPort {

    private final CreateAccountHoldUseCase createAccountHoldUseCase;

    @Override
    public HoldResult createHold(
            UUID accountId,
            UUID authorizationId,
            BigDecimal amount,
            Currency currency
    ) {
        CreateAccountHoldResponse response =
                createAccountHoldUseCase.execute(
                        new CreateAccountHoldCommand(
                                accountId,
                                amount,
                                currency,
                                AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                                AccountHoldReferenceType.CARD_AUTHORIZATION,
                                authorizationId
                        )
                );

        return new HoldResult(
                response.holdId()
        );
    }
}