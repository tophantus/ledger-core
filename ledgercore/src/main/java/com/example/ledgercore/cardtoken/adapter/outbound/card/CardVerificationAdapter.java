package com.example.ledgercore.cardtoken.adapter.outbound.card;

import com.example.ledgercore.card.command.dto.VerifyCardCredentialsCommand;
import com.example.ledgercore.card.command.dto.VerifyCardCredentialsResult;
import com.example.ledgercore.card.command.port.inbound.VerifyCardCredentialsUseCase;
import com.example.ledgercore.cardtoken.command.port.outbound.CardVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardVerificationAdapter
        implements CardVerificationPort {

    private final VerifyCardCredentialsUseCase
            verifyCardCredentialsUseCase;

    @Override
    public CardVerificationResult verify(
            String pan,
            Short expiryMonth,
            Short expiryYear,
            String cvv
    ) {
        VerifyCardCredentialsResult result =
                verifyCardCredentialsUseCase.execute(
                        new VerifyCardCredentialsCommand(
                                pan,
                                expiryMonth,
                                expiryYear,
                                cvv
                        )
                );

        return new CardVerificationResult(
                result.cardId()
        );
    }
}