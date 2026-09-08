package com.example.ledgercore.withdrawal.adapter.outbound.otp;

import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
import com.example.ledgercore.account.query.port.inbound.GetWithdrawAccountInfoUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountWithdrawPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountInfo;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawalAccountAdapter implements WithdrawalAccountPort {

    private final GetWithdrawAccountInfoUseCase getWithdrawAccountInfoUseCase;
    @Override
    public WithdrawalAccountInfo getWithdrawalInfo(UUID userId, UUID accountId) {
        AccountWithdrawInfo info =
                getWithdrawAccountInfoUseCase.execute(
                        userId,
                        accountId
                );

        return new WithdrawalAccountInfo(
                info.accountId(),
                info.currency(),
                info.balance()
        );
    }
}
