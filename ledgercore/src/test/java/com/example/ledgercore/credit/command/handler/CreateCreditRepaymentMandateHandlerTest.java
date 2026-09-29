package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.port.outbound.VerifyRepaymentAccountOwnershipPort;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.RepaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditRepaymentMandateHandlerTest {

    @Mock
    private CreditRepaymentMandateCommandRepository mandateRepository;

    @Mock
    private VerifyRepaymentAccountOwnershipPort ownershipPort;

    @InjectMocks
    private CreateCreditRepaymentMandateHandler handler;

    private UUID userId;
    private UUID creditFacilityId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void execute_shouldCreateActiveMandate_whenCommandIsValid() {
        CreateCreditRepaymentMandateCommand command = validCommand();
        givenAccountIsOwned();
        when(mandateRepository.existsByCreditFacilityIdAndAccountIdAndStatus(
                creditFacilityId,
                accountId,
                CreditRepaymentMandateStatus.ACTIVE
        )).thenReturn(false);

        CreateCreditRepaymentMandateResult result = handler.execute(command);

        ArgumentCaptor<CreditRepaymentMandate> mandateCaptor =
                ArgumentCaptor.forClass(CreditRepaymentMandate.class);
        verify(mandateRepository).save(mandateCaptor.capture());

        CreditRepaymentMandate mandate = mandateCaptor.getValue();
        assertNotNull(mandate.getId());
        assertEquals(creditFacilityId, mandate.getCreditFacilityId());
        assertEquals(accountId, mandate.getAccountId());
        assertEquals(RepaymentType.MINIMUM_PAYMENT, mandate.getRepaymentType());
        assertEquals(CreditRepaymentMandateStatus.ACTIVE, mandate.getStatus());
        assertNotNull(mandate.getCreatedAt());

        assertEquals(mandate.getId(), result.mandateId());
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(accountId, result.accountId());
        assertEquals(RepaymentType.MINIMUM_PAYMENT, result.repaymentType());
        assertEquals(CreditRepaymentMandateStatus.ACTIVE, result.status());
        assertEquals(mandate.getCreatedAt(), result.createdAt());

        verify(ownershipPort).verify(userId, accountId);
        verify(mandateRepository)
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        creditFacilityId,
                        accountId,
                        CreditRepaymentMandateStatus.ACTIVE
                );
    }

    @Test
    void execute_shouldThrow_whenCommandIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verifyNoInteractions(ownershipPort, mandateRepository);
    }

    @Test
    void execute_shouldThrow_whenCommandHasMissingField() {
        CreateCreditRepaymentMandateCommand command =
                new CreateCreditRepaymentMandateCommand(
                        userId,
                        creditFacilityId,
                        accountId,
                        null
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verifyNoInteractions(ownershipPort, mandateRepository);
    }

    @Test
    void execute_shouldThrow_whenAccountIsNotOwnedByUser() {
        when(ownershipPort.verify(userId, accountId)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(validCommand())
        );

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        verify(ownershipPort).verify(userId, accountId);
        verifyNoInteractions(mandateRepository);
    }

    @Test
    void execute_shouldThrow_whenActiveMandateAlreadyExists() {
        givenAccountIsOwned();
        when(mandateRepository.existsByCreditFacilityIdAndAccountIdAndStatus(
                creditFacilityId,
                accountId,
                CreditRepaymentMandateStatus.ACTIVE
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(validCommand())
        );

        assertEquals(
                ErrorCode.CREDIT_REPAYMENT_MANDATE_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(mandateRepository, never()).save(any(CreditRepaymentMandate.class));
    }

    private CreateCreditRepaymentMandateCommand validCommand() {
        return new CreateCreditRepaymentMandateCommand(
                userId,
                creditFacilityId,
                accountId,
                RepaymentType.MINIMUM_PAYMENT
        );
    }

    private void givenAccountIsOwned() {
        when(ownershipPort.verify(userId, accountId)).thenReturn(true);
    }
}
