package com.example.ledgercore.card.adapter.outbound.hold;

import com.example.ledgercore.card.command.port.outbound.CardAuthorizationHoldPort;
import com.example.ledgercore.card.enums.CardAuthorizationHoldType;
import com.example.ledgercore.hold.command.dto.ReleaseAccountHoldCommand;
import com.example.ledgercore.hold.command.dto.ReleaseCreditHoldCommand;
import com.example.ledgercore.hold.command.port.inbound.ReleaseAccountHoldUseCase;
import com.example.ledgercore.hold.command.port.inbound.ReleaseCreditHoldUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardAuthorizationHoldAdapter
        implements CardAuthorizationHoldPort {

    private final ReleaseAccountHoldUseCase
            releaseAccountHoldUseCase;

    private final ReleaseCreditHoldUseCase
            releaseCreditHoldUseCase;

    @Override
    public void releaseHold(
            CardAuthorizationHoldType holdType,
            UUID holdId
    ) {
        switch (holdType) {
            case ACCOUNT ->
                    releaseAccountHoldUseCase.execute(
                            new ReleaseAccountHoldCommand(
                                    holdId
                            )
                    );

            case CREDIT ->
                    releaseCreditHoldUseCase.execute(
                            new ReleaseCreditHoldCommand(
                                    holdId
                            )
                    );
        }
    }
}