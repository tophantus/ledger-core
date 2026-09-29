
package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeCreditRepaymentMandateHandlerTest {

    @Mock
    private CreditRepaymentMandateCommandRepository mandateRepository;

    @Mock
    private CreditFacilityCommandRepository creditFacilityRepository;

    @Mock
    private CreditRepaymentMandate mandate;

    @Mock
    private CreditFacility creditFacility;

    @InjectMocks
    private RevokeCreditRepaymentMandateHandler handler;

    private UUID userId;
    private UUID mandateId;
    private UUID creditFacilityId;
    private UUID accountId;

    private Instant revokedAt;

    private RevokeCreditRepaymentMandateCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mandateId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        revokedAt = Instant.parse("2026-09-30T00:00:00Z");

        command = new RevokeCreditRepaymentMandateCommand(
                userId,
                mandateId
        );
    }

    // --------------------------------------------------
    // Common stubbing
    // --------------------------------------------------

    private void mockExistingMandate() {
        when(mandateRepository.findById(mandateId))
                .thenReturn(Optional.of(mandate));
    }

    private void mockExistingCreditFacility() {
        when(mandate.getCreditFacilityId())
                .thenReturn(creditFacilityId);

        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(creditFacility));
    }

    private void mockAuthorizedUser() {
        when(creditFacility.getCustomerId())
                .thenReturn(userId);
    }

    private void mockActiveMandate() {
        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.ACTIVE);
    }

    private void mockSuccessfulExecution() {
        mockExistingMandate();
        mockExistingCreditFacility();
        mockAuthorizedUser();
        mockActiveMandate();

        when(mandate.getId())
                .thenReturn(mandateId);

        when(mandate.getAccountId())
                .thenReturn(accountId);

        when(mandate.getRevokedAt())
                .thenReturn(revokedAt);
    }

    // --------------------------------------------------
    // Successful execution
    // --------------------------------------------------

    @Test
    void execute_shouldRevokeMandateAndReturnResult_whenCommandIsValid() {
        // Arrange
        mockSuccessfulExecution();

        // After revoke, the mandate should expose its revoked status.
        when(mandate.getStatus())
                .thenReturn(
                        CreditRepaymentMandateStatus.ACTIVE,
                        CreditRepaymentMandateStatus.REVOKED
                );

        // Act
        RevokeCreditRepaymentMandateResult result =
                handler.execute(command);

        // Assert
        assertNotNull(result);
        assertEquals(mandateId, result.mandateId());
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(accountId, result.accountId());
        assertEquals(
                CreditRepaymentMandateStatus.REVOKED,
                result.status()
        );
        assertEquals(revokedAt, result.revokedAt());

        verify(mandateRepository)
                .findById(mandateId);

        verify(mandate)
                .getCreditFacilityId();

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verify(mandate)
                .revoke();

        verify(mandateRepository)
                .save(mandate);

        verify(mandate)
                .getId();

        verify(mandate)
                .getCreditFacilityId();

        verify(mandate)
                .getAccountId();

        verify(mandate)
                .getRevokedAt();

        verifyNoMoreInteractions(
                mandateRepository,
                creditFacilityRepository,
                mandate,
                creditFacility
        );
    }

    // --------------------------------------------------
    // Command validation
    // --------------------------------------------------

    @Test
    void execute_shouldThrowBusinessException_whenCommandIsNull() {
        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        verifyNoInteractions(
                mandateRepository,
                creditFacilityRepository,
                mandate,
                creditFacility
        );
    }

    @Test
    void execute_shouldThrowBusinessException_whenUserIdIsNull() {
        // Arrange
        RevokeCreditRepaymentMandateCommand invalidCommand =
                new RevokeCreditRepaymentMandateCommand(
                        null,
                        mandateId
                );

        // Act & Assert
        assertInvalidCommand(invalidCommand);
    }

    @Test
    void execute_shouldThrowBusinessException_whenMandateIdIsNull() {
        // Arrange
        RevokeCreditRepaymentMandateCommand invalidCommand =
                new RevokeCreditRepaymentMandateCommand(
                        userId,
                        null
                );

        // Act & Assert
        assertInvalidCommand(invalidCommand);
    }

    private void assertInvalidCommand(
            RevokeCreditRepaymentMandateCommand invalidCommand
    ) {
        assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        verifyNoInteractions(
                mandateRepository,
                creditFacilityRepository,
                mandate,
                creditFacility
        );
    }

    // --------------------------------------------------
    // Mandate not found
    // --------------------------------------------------

    @Test
    void execute_shouldThrowBusinessException_whenMandateDoesNotExist() {
        // Arrange
        when(mandateRepository.findById(mandateId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandateRepository)
                .findById(mandateId);

        verifyNoMoreInteractions(mandateRepository);

        verifyNoInteractions(
                creditFacilityRepository,
                mandate,
                creditFacility
        );
    }

    // --------------------------------------------------
    // Credit facility not found
    // --------------------------------------------------

    @Test
    void execute_shouldThrowBusinessException_whenCreditFacilityDoesNotExist() {
        // Arrange
        mockExistingMandate();

        when(mandate.getCreditFacilityId())
                .thenReturn(creditFacilityId);

        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandateRepository)
                .findById(mandateId);

        verify(mandate)
                .getCreditFacilityId();

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verifyNoMoreInteractions(
                mandateRepository,
                creditFacilityRepository,
                mandate
        );

        verifyNoInteractions(creditFacility);
    }

    // --------------------------------------------------
    // Authorization
    // --------------------------------------------------

    @Test
    void execute_shouldThrowBusinessException_whenUserDoesNotOwnCreditFacility() {
        // Arrange
        UUID anotherUserId = UUID.randomUUID();

        mockExistingMandate();
        mockExistingCreditFacility();

        when(creditFacility.getCustomerId())
                .thenReturn(anotherUserId);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandateRepository)
                .findById(mandateId);

        verify(mandate)
                .getCreditFacilityId();

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verify(mandate, never())
                .getStatus();

        verify(mandate, never())
                .revoke();

        verify(mandateRepository, never())
                .save(any());

        verifyNoMoreInteractions(
                mandateRepository,
                creditFacilityRepository,
                mandate,
                creditFacility
        );
    }

    // --------------------------------------------------
    // Mandate status
    // --------------------------------------------------

    @Test
    void execute_shouldThrowBusinessException_whenMandateIsNotActive() {
        // Arrange
        mockExistingMandate();
        mockExistingCreditFacility();
        mockAuthorizedUser();

        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.REVOKED);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandateRepository)
                .findById(mandateId);

        verify(mandate)
                .getCreditFacilityId();

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verify(mandate)
                .getStatus();

        verify(mandate, never())
                .revoke();

        verify(mandateRepository, never())
                .save(any());

        verifyNoMoreInteractions(
                mandateRepository,
                creditFacilityRepository,
                mandate,
                creditFacility
        );
    }

    // --------------------------------------------------
    // Revoke and persistence behavior
    // --------------------------------------------------

    @Test
    void execute_shouldRevokeBeforeSaving_whenCommandIsValid() {
        // Arrange
        mockSuccessfulExecution();

        when(mandate.getStatus())
                .thenReturn(
                        CreditRepaymentMandateStatus.ACTIVE,
                        CreditRepaymentMandateStatus.REVOKED
                );

        // Act
        handler.execute(command);

        // Assert
        var order = inOrder(mandate, mandateRepository);

        order.verify(mandate)
                .revoke();

        order.verify(mandateRepository)
                .save(mandate);
    }

    @Test
    void execute_shouldSaveSameMandateInstance_whenRevocationSucceeds() {
        // Arrange
        mockSuccessfulExecution();

        when(mandate.getStatus())
                .thenReturn(
                        CreditRepaymentMandateStatus.ACTIVE,
                        CreditRepaymentMandateStatus.REVOKED
                );

        // Act
        handler.execute(command);

        // Assert
        verify(mandateRepository)
                .save(same(mandate));
    }

    @Test
    void execute_shouldNotSaveMandate_whenRevocationIsRejected() {
        // Arrange
        mockExistingMandate();
        mockExistingCreditFacility();
        mockAuthorizedUser();

        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.REVOKED);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandate, never())
                .revoke();

        verify(mandateRepository, never())
                .save(any());
    }
}