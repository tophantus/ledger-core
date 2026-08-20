package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CreateAccountCommand;
import com.example.ledgercore.account.command.port.inbound.CreateAccountUseCase;
import com.example.ledgercore.account.command.port.outbound.AccountNumberGeneratorPort;
import com.example.ledgercore.account.command.port.outbound.LedgerAccountPort;
import com.example.ledgercore.account.command.port.outbound.UserAccountPort;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAccountHandler implements CreateAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;
    private final AccountNumberGeneratorPort accountNumberGeneratorPort;
    private final UserAccountPort userAccountPort;
    private final LedgerAccountPort ledgerAccountPort;

    @Override
    @Transactional
    public AccountResponse execute(CreateAccountCommand command) {
        UUID userId = command.userId();

        if (!userAccountPort.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String accountNo = accountNumberGeneratorPort.generate();

        UUID ledgerAccountId = ledgerAccountPort.createCustomerAccount(
                accountNo,
                command.currency()
        );

        Account account = Account.builder()
                .userId(userId)
                .accountNo(accountNo)
                .currency(command.currency())
                .ledgerAccountId(ledgerAccountId)
                .build();

        Account savedAccount = accountCommandRepository.save(account);

        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getUserId(),
                savedAccount.getAccountNo(),
                savedAccount.getCurrency(),
                savedAccount.getBalance(),
                savedAccount.getStatus(),
                savedAccount.getCreatedAt(),
                savedAccount.getUpdatedAt()
        );
    }
}