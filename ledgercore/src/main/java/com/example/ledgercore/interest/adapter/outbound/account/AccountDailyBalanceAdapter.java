package com.example.ledgercore.interest.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.GetAccountDailyBalanceUseCase;
import com.example.ledgercore.interest.command.port.outbound.AccountDailyBalanceInfo;
import com.example.ledgercore.interest.command.port.outbound.AccountDailyBalancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountDailyBalanceAdapter
        implements AccountDailyBalancePort {

    private final GetAccountDailyBalanceUseCase useCase;

    @Override
    public AccountDailyBalanceInfo findClosingBalance(
            UUID accountId,
            LocalDate businessDate
    ) {
        var response =
                useCase.execute(
                        accountId,
                        businessDate
                );

        return new AccountDailyBalanceInfo(
                response.accountId(),
                response.businessDate(),
                response.closingBalance()
        );
    }
}