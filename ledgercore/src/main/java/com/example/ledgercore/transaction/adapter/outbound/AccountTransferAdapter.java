package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;
import com.example.ledgercore.account.command.port.inbound.TransferAccountBalanceUseCase;
import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.port.inbound.GetAccountIdByAccountNoUseCase;
import com.example.ledgercore.account.query.port.inbound.GetTransferAccountInfoUseCase;
import com.example.ledgercore.account.query.port.inbound.CheckAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountTransferAdapter
        implements AccountTransferPort {

    private final GetTransferAccountInfoUseCase
            getTransferAccountInfoUseCase;

    private final GetAccountIdByAccountNoUseCase
            getAccountIdByAccountNoUseCase;

    private final CheckAccountOwnershipUseCase
            checkAccountOwnershipUseCase;

    private final TransferAccountBalanceUseCase
            transferAccountBalanceUseCase;

    @Override
    public AccountTransferPort.TransferAccountInfo getTransferInfo(
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

        return new AccountTransferPort.TransferAccountInfo(
                info.sourceAccountId(),
                info.destinationAccountId(),
                info.currency(),
                info.sourceBalance()
        );
    }

    @Override
    public UUID getAccountIdByAccountNo(String accountNo) {
        return getAccountIdByAccountNoUseCase.execute(accountNo);
    }

    @Override
    public void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    ) {
        boolean isOwner = checkAccountOwnershipUseCase.execute(
                userId,
                sourceAccountId
        );

        if (!isOwner) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    public void transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount
    ) {
        transferAccountBalanceUseCase.execute(
                new TransferAccountCommand(
                        sourceAccountId,
                        destinationAccountId,
                        amount
                )
        );
    }
}