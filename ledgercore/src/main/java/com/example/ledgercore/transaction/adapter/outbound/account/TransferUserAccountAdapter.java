package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;
import com.example.ledgercore.account.command.port.inbound.TransferAccountBalanceUseCase;
import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.port.inbound.GetAccountIdByAccountNoUseCase;
import com.example.ledgercore.account.query.port.inbound.GetTransferAccountInfoUseCase;
import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferUserAccountAdapter
        implements TransferUserAccountPort {

    private final GetTransferAccountInfoUseCase
            getTransferAccountInfoUseCase;

    private final GetAccountIdByAccountNoUseCase
            getAccountIdByAccountNoUseCase;

    private final CheckUserAccountOwnershipUseCase
            checkUserAccountOwnershipUseCase;

    private final TransferAccountBalanceUseCase
            transferAccountBalanceUseCase;

    @Override
    public TransferUserAccountPort.TransferAccountInfo getTransferInfo(
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

        return new TransferUserAccountPort.TransferAccountInfo(
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

    @Override
    public void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    ) {
        boolean isOwner = checkUserAccountOwnershipUseCase.execute(
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
            BigDecimal amount,
            LocalDate businessDate
    ) {
        transferAccountBalanceUseCase.execute(
                new TransferAccountCommand(
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        businessDate
                )
        );
    }
}