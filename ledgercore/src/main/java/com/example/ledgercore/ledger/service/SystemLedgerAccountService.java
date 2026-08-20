package com.example.ledgercore.ledger.service;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.config.SystemLedgerAccountProperties;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.LedgerAccountStatus;
import com.example.ledgercore.ledger.query.repository.LedgerAccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemLedgerAccountService {

    private final LedgerAccountQueryRepository ledgerAccountQueryRepository;
    private final SystemLedgerAccountProperties properties;

    @Transactional(readOnly = true)
    public LedgerAccount getDepositSource(String currency) {
        String code = properties.getDepositCodes()
                .get(currency);

        if (code == null) {
            throw new BusinessException(
                    ErrorCode.LEDGER_ACCOUNT_NOT_FOUND
            );
        }

        LedgerAccount account =
                ledgerAccountQueryRepository
                        .findByCodeAndCurrency(
                                code,
                                currency
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.LEDGER_ACCOUNT_NOT_FOUND
                                )
                        );

        if (account.getStatus()
                != LedgerAccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.LEDGER_ACCOUNT_NOT_ACTIVE
            );
        }

        return account;
    }
}