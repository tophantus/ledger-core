package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.port.outbound.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetAccountQuery;
import com.example.ledgercore.account.query.port.inbound.GetAccountUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountHandler implements GetAccountUseCase {

    private final AccountQueryRepository accountQueryRepository;
    private final ProductAccountPort productAccountPort;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse execute(GetAccountQuery query) {
        Account account = accountQueryRepository.findById(query.accountId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
                );

        if (!account.getUserId().equals(query.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        ProductAccountInfo product =
                productAccountPort.getActiveProduct(
                        account.getProductId()
                );

        return toResponse(account, product.code());
    }

    private AccountResponse toResponse(Account account, String productCode) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNo(),
                productCode,
                account.getCurrency(),
                account.getBalance().toPlainString(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}