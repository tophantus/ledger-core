package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.ledger.command.dto.CreateLedgerAccountCommand;
import com.example.ledgercore.ledger.command.repository.LedgerAccountCommandRepository;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.LedgerAccountStatus;
import com.example.ledgercore.ledger.enums.LedgerAccountType;
import com.example.ledgercore.ledger.service.LedgerAccountCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateLedgerAccountHandlerTest {

    @Mock
    private LedgerAccountCommandRepository ledgerAccountCommandRepository;

    @Mock
    private LedgerAccountCodeService ledgerAccountCodeService;

    private CreateLedgerAccountHandler handler;

    private String accountNo;
    private String currency;

    @BeforeEach
    void setUp() {
        handler = new CreateLedgerAccountHandler(
                ledgerAccountCommandRepository,
                ledgerAccountCodeService
        );

        accountNo = "1000000001";
        currency = "VND";
    }

    @Test
    void shouldCreateCustomerLedgerAccount() {
        String code = "CUSTOMER-1000000001";
        String name = "Customer Account 1000000001";

        when(ledgerAccountCodeService.generateCustomerCode(accountNo))
                .thenReturn(code);

        when(ledgerAccountCodeService.generateCustomerName(accountNo))
                .thenReturn(name);

        when(ledgerAccountCommandRepository.save(any(LedgerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(
                new CreateLedgerAccountCommand(
                        accountNo,
                        currency
                )
        );

        ArgumentCaptor<LedgerAccount> captor =
                ArgumentCaptor.forClass(LedgerAccount.class);

        verify(ledgerAccountCommandRepository)
                .save(captor.capture());

        LedgerAccount account = captor.getValue();

        assertEquals(code, account.getCode());
        assertEquals(name, account.getName());
        assertEquals(LedgerAccountType.LIABILITY, account.getType());
        assertEquals(currency, account.getCurrency());
        assertEquals(LedgerAccountStatus.ACTIVE, account.getStatus());

        verify(ledgerAccountCodeService)
                .generateCustomerCode(accountNo);

        verify(ledgerAccountCodeService)
                .generateCustomerName(accountNo);
    }

    @Test
    void shouldGenerateCodeAndNameFromAccountNumber() {
        String code = "CUSTOMER-001";
        String name = "Customer 001";

        when(ledgerAccountCodeService.generateCustomerCode(accountNo))
                .thenReturn(code);

        when(ledgerAccountCodeService.generateCustomerName(accountNo))
                .thenReturn(name);

        when(ledgerAccountCommandRepository.save(any(LedgerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(
                new CreateLedgerAccountCommand(
                        accountNo,
                        currency
                )
        );

        verify(ledgerAccountCodeService)
                .generateCustomerCode(accountNo);

        verify(ledgerAccountCodeService)
                .generateCustomerName(accountNo);
    }
}