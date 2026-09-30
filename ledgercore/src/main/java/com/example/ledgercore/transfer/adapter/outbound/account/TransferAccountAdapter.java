package com.example.ledgercore.transfer.adapter.outbound.account;

import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.port.inbound.GetAccountIdByAccountNoUseCase;
import com.example.ledgercore.account.query.port.inbound.GetTransferAccountInfoUseCase;
import com.example.ledgercore.transfer.command.port.outbound.TransferAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferAccountAdapter
        implements TransferAccountPort {

    private final GetTransferAccountInfoUseCase
            getTransferAccountInfoUseCase;

    private final GetAccountIdByAccountNoUseCase
            getAccountIdByAccountNoUseCase;

    @Override
    public TransferAccountInfo getTransferInfo(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        AccountTransferInfo info =
                getTransferAccountInfoUseCase.execute(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        return new TransferAccountInfo(
                info.sourceAccountId(),
                info.destinationAccountId(),
                info.currency(),
                info.sourceAvailableBalance()
        );
    }

    @Override
    public UUID getAccountIdByAccountNo(String accountNo) {
        return getAccountIdByAccountNoUseCase.execute(accountNo);
    }
}