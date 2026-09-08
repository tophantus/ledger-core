package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CreateAccountCommand;
import com.example.ledgercore.account.command.port.inbound.CreateAccountUseCase;
import com.example.ledgercore.account.command.port.outbound.*;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.port.outbound.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAccountHandler
        implements CreateAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;
    private final AccountNumberGeneratorPort accountNumberGeneratorPort;

    private final UserAccountPort userAccountPort;

    private final LedgerAccountPort ledgerAccountPort;

    private final ProductAccountPort productAccountPort;


    @Override
    @Transactional
    public AccountResponse execute(
            CreateAccountCommand command
    ) {
        validateCommand(command);

        UUID userId = command.userId();

        if (!userAccountPort.existsById(userId)) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        ProductAccountInfo product =
                productAccountPort.getActiveProduct(
                        command.productId()
                );

        String accountNo =
                accountNumberGeneratorPort.generate();

        UUID ledgerAccountId =
                ledgerAccountPort.createCustomerAccount(
                        accountNo,
                        command.currency()
                );

        Account account =
                Account.builder()
                        .userId(userId)
                        .productId(product.productId())
                        .accountNo(accountNo)
                        .currency(command.currency())
                        .ledgerAccountId(ledgerAccountId)
                        .build();

        Account savedAccount =
                accountCommandRepository.save(account);

        return AccountMapper.toResponse(
                savedAccount
        );
    }

    private void validateCommand(
            CreateAccountCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.userId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.productId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.currency() == null
                || command.currency().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}