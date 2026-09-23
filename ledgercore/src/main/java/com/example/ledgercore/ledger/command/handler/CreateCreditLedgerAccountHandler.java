package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.ledger.command.dto.CreateCreditLedgerAccountCommand;
import com.example.ledgercore.ledger.command.port.inbound.CreateCreditLedgerAccountUseCase;
import com.example.ledgercore.ledger.command.repository.LedgerAccountCommandRepository;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.LedgerAccountStatus;
import com.example.ledgercore.ledger.enums.LedgerAccountType;
import com.example.ledgercore.ledger.service.LedgerAccountCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCreditLedgerAccountHandler
        implements CreateCreditLedgerAccountUseCase {

    private final LedgerAccountCommandRepository
            ledgerAccountCommandRepository;

    private final LedgerAccountCodeService
            ledgerAccountCodeService;

    @Override
    @Transactional
    public UUID execute(
            CreateCreditLedgerAccountCommand command
    ) {
        String code =
                ledgerAccountCodeService
                        .generateCreditCode(
                                command.creditFacilityId()
                        );

        String name =
                ledgerAccountCodeService
                        .generateCreditName(
                                command.creditFacilityId()
                        );

        LedgerAccount ledgerAccount =
                LedgerAccount.builder()
                        .code(code)
                        .name(name)
                        .type(LedgerAccountType.ASSET)
                        .currency(command.currency())
                        .status(LedgerAccountStatus.ACTIVE)
                        .build();

        LedgerAccount savedLedgerAccount =
                ledgerAccountCommandRepository.save(
                        ledgerAccount
                );

        return savedLedgerAccount.getId();
    }
}