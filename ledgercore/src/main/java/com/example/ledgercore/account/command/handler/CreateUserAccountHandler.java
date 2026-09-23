package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CreatUserAccountCommand;
import com.example.ledgercore.account.command.port.inbound.CreateUserAccountUseCase;
import com.example.ledgercore.account.command.port.outbound.*;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.repository.UserAccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.entity.UserAccount;
import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.port.outbound.dto.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.AccountProductPort;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.enums.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateUserAccountHandler
        implements CreateUserAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;
    private final UserAccountCommandRepository userAccountCommandRepository;
    private final AccountNumberGeneratorPort accountNumberGeneratorPort;

    private final AccountUserPort accountUserPort;

    private final AccountLedgerPort accountLedgerPort;

    private final AccountProductPort accountProductPort;

    @Override
    @Transactional
    public AccountResponse execute(
            CreatUserAccountCommand command
    ) {
        validateCommand(command);

        UUID userId = command.userId();

        if (!accountUserPort.existsById(userId)) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        ProductAccountInfo product =
                accountProductPort.getActiveProduct(
                        command.productId()
                );

        if (product.type() != ProductType.DEPOSIT) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_PRODUCT_TYPE_INVALID
            );
        }

        String accountNo =
                accountNumberGeneratorPort.generate();

        UUID ledgerAccountId =
                accountLedgerPort.createLedgerAccount(
                        accountNo,
                        command.currency()
                );

        Account account =
                Account.builder()
                        .productId(product.productId())
                        .accountNo(accountNo)
                        .currency(command.currency())
                        .ledgerAccountId(ledgerAccountId)
                        .build();

        Account savedAccount =
                accountCommandRepository.save(account);

        UserAccount userAccount =
                UserAccount.builder()
                        .userId(userId)
                        .accountId(savedAccount.getId())
                        .build();

        userAccountCommandRepository.save(userAccount);

        return AccountMapper.toResponse(
                savedAccount
        );
    }

    private void validateCommand(
            CreatUserAccountCommand command
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

        if (command.currency() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}