package com.example.ledgercore.credit.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
import com.example.ledgercore.credit.command.port.outbound.VerifyRepaymentAccountOwnershipPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RepaymentAccountOwnershipAdapter
        implements VerifyRepaymentAccountOwnershipPort {

    private final CheckUserAccountOwnershipUseCase checkUserAccountOwnershipUseCase;

    @Override
    public boolean verify(
            UUID userId,
            UUID accountId
    ) {
        return checkUserAccountOwnershipUseCase.execute(
                userId,
                accountId
        );
    }
}