package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CreateProviderAccountCommand;
import com.example.ledgercore.account.command.port.inbound.CreateProviderAccountUseCase;
import com.example.ledgercore.account.command.port.outbound.AccountNumberGeneratorPort;
import com.example.ledgercore.account.command.port.outbound.AccountLedgerPort;
import com.example.ledgercore.account.command.port.outbound.AccountProviderPort;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.repository.ProviderAccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.entity.ProviderAccount;
import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.port.outbound.AccountProductPort;
import com.example.ledgercore.account.port.outbound.dto.ProductAccountInfo;
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
public class CreateProviderAccountHandler
        implements CreateProviderAccountUseCase {

    private final AccountCommandRepository accountCommandRepository;
    private final ProviderAccountCommandRepository providerAccountCommandRepository;

    private final AccountProviderPort accountProviderPort;
    private final AccountProductPort accountProductPort;
    private final AccountNumberGeneratorPort accountNumberGeneratorPort;
    private final AccountLedgerPort accountLedgerPort;

    @Override
    @Transactional
    public AccountResponse execute(
            CreateProviderAccountCommand command
    ) {
        validateCommand(command);

        UUID providerId = command.providerId();

        if (!accountProviderPort.existsById(providerId)) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_NOT_FOUND
            );
        }

        ProductAccountInfo product =
                accountProductPort.getActiveProviderProduct();

        if (product.type() != ProductType.PROVIDER) {
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

        ProviderAccount providerAccount =
                ProviderAccount.builder()
                        .providerId(providerId)
                        .accountId(savedAccount.getId())
                        .build();

        providerAccountCommandRepository.save(providerAccount);

        return AccountMapper.toResponse(
                savedAccount
        );
    }

    private void validateCommand(
            CreateProviderAccountCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.providerId() == null) {
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