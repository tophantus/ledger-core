package com.example.ledgercore.card.adapter.outbound;

import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountQuery;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetActiveOwnedAccountUseCase;
import com.example.ledgercore.card.command.port.outbound.AccountPort;
import com.example.ledgercore.card.command.port.outbound.dto.AccountInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountPortAdapter
        implements AccountPort {

    private final GetActiveOwnedAccountUseCase getActiveOwnedAccountUseCase;

    @Override
    public AccountInfo getOwnedAccount(
            UUID customerId,
            UUID accountId
    ) {
        GetActiveOwnedAccountResult result =
                getActiveOwnedAccountUseCase.execute(
                        new GetActiveOwnedAccountQuery(
                                customerId,
                                accountId
                        )
                );

        return new AccountInfo(
                result.id(),
                result.userId(),
                result.productId()
        );
    }
}