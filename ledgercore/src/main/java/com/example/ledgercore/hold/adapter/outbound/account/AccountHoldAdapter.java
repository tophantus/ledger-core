package com.example.ledgercore.hold.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.DecreaseAccountHoldCommand;
import com.example.ledgercore.account.command.dto.IncreaseAccountHoldCommand;
import com.example.ledgercore.account.command.port.inbound.DecreaseAccountHoldUseCase;
import com.example.ledgercore.account.command.port.inbound.IncreaseAccountHoldUseCase;
import com.example.ledgercore.hold.command.port.outbound.AccountHoldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountHoldAdapter
        implements AccountHoldPort {

    private final IncreaseAccountHoldUseCase
            increaseAccountHoldUseCase;

    private final DecreaseAccountHoldUseCase
            decreaseAccountHoldUseCase;

    @Override
    public void increaseHold(
            UUID accountId,
            BigDecimal amount,
            String currency
    ) {
        increaseAccountHoldUseCase.execute(
                new IncreaseAccountHoldCommand(
                        accountId,
                        amount,
                        currency
                )
        );
    }

    @Override
    public void decreaseHold(
            UUID accountId,
            BigDecimal amount,
            String currency
    ) {
        decreaseAccountHoldUseCase.execute(
                new DecreaseAccountHoldCommand(
                        accountId,
                        amount,
                        currency
                )
        );
    }
}