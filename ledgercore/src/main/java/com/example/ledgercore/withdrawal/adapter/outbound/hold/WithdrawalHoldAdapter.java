package com.example.ledgercore.withdrawal.adapter.outbound.hold;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldResponse;
import com.example.ledgercore.hold.command.dto.ReleaseAccountHoldCommand;
import com.example.ledgercore.hold.command.port.inbound.CreateAccountHoldUseCase;
import com.example.ledgercore.hold.command.port.inbound.ReleaseAccountHoldUseCase;
import com.example.ledgercore.hold.enums.AccountHoldReferenceType;
import com.example.ledgercore.hold.enums.AccountHoldType;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawalHoldAdapter
        implements WithdrawalHoldPort {

    private final CreateAccountHoldUseCase
            createAccountHoldUseCase;

    private final ReleaseAccountHoldUseCase
            releaseAccountHoldUseCase;

    @Override
    public UUID createHold(
            UUID withdrawalIntentId,
            UUID accountId,
            BigDecimal amount,
            Currency currency
    ) {
        CreateAccountHoldResponse response =
                createAccountHoldUseCase.execute(
                        new CreateAccountHoldCommand(
                                accountId,
                                amount,
                                currency,
                                AccountHoldType
                                        .AVAILABLE_BALANCE_RESERVATION,
                                AccountHoldReferenceType
                                        .WITHDRAWAL_INTENT,
                                withdrawalIntentId
                        )
                );

        return response.holdId();
    }

    @Override
    public void releaseHold(UUID holdId) {
        releaseAccountHoldUseCase.execute(
                new ReleaseAccountHoldCommand(
                        holdId
                )
        );
    }
}