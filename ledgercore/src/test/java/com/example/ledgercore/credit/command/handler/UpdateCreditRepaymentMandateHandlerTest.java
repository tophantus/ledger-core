
package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.RepaymentType;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCreditRepaymentMandateHandlerTest {

    @Mock
    private CreditRepaymentMandateCommandRepository mandateRepository;

    @Mock
    private CreditFacilityCommandRepository creditFacilityRepository;

    @Mock
    private CreditRepaymentMandate mandate;

    @Mock
    private CreditFacility creditFacility;

    @InjectMocks
    private UpdateCreditRepaymentMandateHandler handler;

    private UUID userId;
    private UUID mandateId;
    private UUID creditFacilityId;
    private UUID accountId;

    private RepaymentType repaymentType;
    private RepaymentType updatedRepaymentType;

    private Instant updatedAt;

    private UpdateCreditRepaymentMandateCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mandateId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        repaymentType = RepaymentType.MINIMUM_PAYMENT;
        updatedRepaymentType = RepaymentType.FULL_BALANCE;

        updatedAt = Instant.parse("2026-09-30T00:00:00Z");

        command = new UpdateCreditRepaymentMandateCommand(
                userId,
                mandateId,
                updatedRepaymentType
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

        when(mandate.getRepaymentType())
                .thenReturn(updatedRepaymentType);

        when(mandate.getUpdatedAt())
                .thenReturn(updatedAt);
    }

    // --------------------------------------------------
    // Successful execution
    // --------------------------------------------------

    @Test
    void execute_shouldUpdateMandateAndReturnResult_whenCommandIsValid() {
        // Arrange
        mockSuccessfulExecution();

        // Act
        UpdateCreditRepaymentMandateResult result =
                handler.execute(command);

        // Assert
        assertNotNull(result);
        assertEquals(mandateId, result.mandateId());
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(accountId, result.accountId());
        assertEquals(updatedRepaymentType, result.repaymentType());
        assertEquals(
                CreditRepaymentMandateStatus.ACTIVE,
                result.status()
        );
        assertEquals(updatedAt, result.updatedAt());

        verify(mandateRepository)
                .findById(mandateId);

        verify(mandate)
                .getCreditFacilityId();

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verify(mandate)
                .updateRepaymentType(updatedRepaymentType);

        verify(mandateRepository)
                .save(mandate);

        verify(mandate)
                .getId();

        verify(mandate)
                .getAccountId();

        verify(mandate)
                .getRepaymentType();

        verify(mandate, times(2))
                .getStatus();

        verify(mandate)
                .getUpdatedAt();

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
        UpdateCreditRepaymentMandateCommand invalidCommand =
                new UpdateCreditRepaymentMandateCommand(
                        null,
                        mandateId,
                        updatedRepaymentType
                );

        // Act & Assert
        assertInvalidCommand(invalidCommand);
    }

    @Test
    void execute_shouldThrowBusinessException_whenMandateIdIsNull() {
        // Arrange
        UpdateCreditRepaymentMandateCommand invalidCommand =
                new UpdateCreditRepaymentMandateCommand(
                        userId,
                        null,
                        updatedRepaymentType
                );

        // Act & Assert
        assertInvalidCommand(invalidCommand);
    }

    @Test
    void execute_shouldThrowBusinessException_whenRepaymentTypeIsNull() {
        // Arrange
        UpdateCreditRepaymentMandateCommand invalidCommand =
                new UpdateCreditRepaymentMandateCommand(
                        userId,
                        mandateId,
                        null
                );

        // Act & Assert
        assertInvalidCommand(invalidCommand);
    }

    private void assertInvalidCommand(
            UpdateCreditRepaymentMandateCommand invalidCommand
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
                .updateRepaymentType(any());

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
                .updateRepaymentType(any());

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
    // Persistence and update behavior
    // --------------------------------------------------

    @Test
    void execute_shouldSaveSameMandateInstance_whenUpdateSucceeds() {
        // Arrange
        mockSuccessfulExecution();

        // Act
        handler.execute(command);

        // Assert
        verify(mandate)
                .updateRepaymentType(updatedRepaymentType);

        verify(mandateRepository)
                .save(same(mandate));
    }

    @Test
    void execute_shouldNotSaveMandate_whenUpdateIsRejected() {
        // Arrange
        mockExistingMandate();
        mockExistingCreditFacility();

        when(creditFacility.getCustomerId())
                .thenReturn(userId);

        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.REVOKED);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandateRepository, never())
                .save(any());

        verify(mandate, never())
                .updateRepaymentType(any());
    }
}