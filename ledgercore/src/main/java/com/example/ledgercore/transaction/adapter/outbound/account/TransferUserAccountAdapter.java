package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;
import com.example.ledgercore.account.command.port.inbound.TransferAccountBalanceUseCase;
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

    private final TransferAccountBalanceUseCase
            transferAccountBalanceUseCase;

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