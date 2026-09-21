package com.example.ledgercore.card.adapter.outbound.hold;

import com.example.ledgercore.card.command.port.outbound.CreditAuthorizationHoldPort;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.command.dto.CreateCreditHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateCreditHoldResponse;
import com.example.ledgercore.hold.command.port.inbound.CreateCreditHoldUseCase;
import com.example.ledgercore.hold.enums.CreditHoldReferenceType;
import com.example.ledgercore.hold.enums.CreditHoldType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditAuthorizationHoldAdapter
        implements CreditAuthorizationHoldPort {

    private final CreateCreditHoldUseCase createCreditHoldUseCase;

    @Override
    public CreditHoldResult createHold(
            UUID creditFacilityId,
            UUID authorizationId,
            String reference,
            BigDecimal amount,
            Currency currency
    ) {
        CreateCreditHoldResponse response =
                createCreditHoldUseCase.execute(
                        new CreateCreditHoldCommand(
                                creditFacilityId,
                                amount,
                                currency,
                                CreditHoldType.AVAILABLE_CREDIT_RESERVATION,
                                CreditHoldReferenceType.CARD_AUTHORIZATION,
                                authorizationId
                        )
                );

        return new CreditHoldResult(
                response.holdId()
        );
    }
}