
package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateResult;
import com.example.ledgercore.credit.command.port.outbound.VerifyRepaymentAccountOwnershipPort;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditRepaymentMandateCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCreditRepaymentMandateHandlerTest {

    @Mock
    private CreditRepaymentMandateCommandRepository mandateRepository;

    @Mock
    private CreditFacilityCommandRepository creditFacilityRepository;

    @Mock
    private VerifyRepaymentAccountOwnershipPort ownershipPort;

    @Mock
    private CreditFacility creditFacility;

    @InjectMocks
    private CreateCreditRepaymentMandateHandler handler;

    private UUID userId;
    private UUID creditFacilityId;
    private UUID accountId;

    private CreateCreditRepaymentMandateCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        command = validCommand();
    }

    // --------------------------------------------------
    // Common stubbing
    // --------------------------------------------------

    private void mockExistingCreditFacility() {
        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(creditFacility));
    }

    private void mockAuthorizedCustomer() {
        when(creditFacility.getCustomerId())
                .thenReturn(userId);
    }

    private void mockOwnedAccount() {
        when(ownershipPort.verify(userId, accountId))
                .thenReturn(true);
    }

    private void mockNoActiveMandate() {
        when(mandateRepository
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        creditFacilityId,
                        accountId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(false);
    }

    private void mockSuccessfulExecution() {
        mockExistingCreditFacility();
        mockAuthorizedCustomer();
        mockOwnedAccount();
        mockNoActiveMandate();
    }

    // --------------------------------------------------
    // Successful execution
    // --------------------------------------------------

    @Test
    void execute_shouldCreateActiveMandate_whenCommandIsValid() {
        // Arrange
        mockSuccessfulExecution();

        // Act
        CreateCreditRepaymentMandateResult result =
                handler.execute(command);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<CreditRepaymentMandate> captor =
                ArgumentCaptor.forClass(CreditRepaymentMandate.class);

        verify(mandateRepository).save(captor.capture());

        CreditRepaymentMandate saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals(creditFacilityId, saved.getCreditFacilityId());
        assertEquals(accountId, saved.getAccountId());
        assertEquals(
                RepaymentType.MINIMUM_PAYMENT,
                saved.getRepaymentType()
        );
        assertEquals(
                CreditRepaymentMandateStatus.ACTIVE,
                saved.getStatus()
        );

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt());

        assertEquals(saved.getId(), result.mandateId());
        assertEquals(
                saved.getCreditFacilityId(),
                result.creditFacilityId()
        );
        assertEquals(saved.getAccountId(), result.accountId());
        assertEquals(
                saved.getRepaymentType(),
                result.repaymentType()
        );
        assertEquals(saved.getStatus(), result.status());
        assertEquals(saved.getCreatedAt(), result.createdAt());

        verify(creditFacilityRepository)
                .findById(creditFacilityId);
        verify(creditFacility)
                .getCustomerId();

        verify(ownershipPort)
                .verify(userId, accountId);

        verify(mandateRepository)
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        creditFacilityId,
                        accountId,
                        CreditRepaymentMandateStatus.ACTIVE
                );
    }

    @Test
    void execute_shouldCreateMandateWithRequestedRepaymentType() {
        // Arrange
        CreateCreditRepaymentMandateCommand customCommand =
                new CreateCreditRepaymentMandateCommand(
                        userId,
                        creditFacilityId,
                        accountId,
                        RepaymentType.FULL_BALANCE
                );

        mockSuccessfulExecution();

        // Act
        CreateCreditRepaymentMandateResult result =
                handler.execute(customCommand);

        // Assert
        assertEquals(
                RepaymentType.FULL_BALANCE,
                result.repaymentType()
        );

        ArgumentCaptor<CreditRepaymentMandate> captor =
                ArgumentCaptor.forClass(CreditRepaymentMandate.class);

        verify(mandateRepository).save(captor.capture());

        assertEquals(
                RepaymentType.FULL_BALANCE,
                captor.getValue().getRepaymentType()
        );
    }

    // --------------------------------------------------
    // Command validation
    // --------------------------------------------------

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        // Act
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        // Assert
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityRepository,
                mandateRepository,
                ownershipPort
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenUserIdIsNull() {
        assertInvalidCommand(
                new CreateCreditRepaymentMandateCommand(
                        null,
                        creditFacilityId,
                        accountId,
                        RepaymentType.MINIMUM_PAYMENT
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCreditFacilityIdIsNull() {
        assertInvalidCommand(
                new CreateCreditRepaymentMandateCommand(
                        userId,
                        null,
                        accountId,
                        RepaymentType.MINIMUM_PAYMENT
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenAccountIdIsNull() {
        assertInvalidCommand(
                new CreateCreditRepaymentMandateCommand(
                        userId,
                        creditFacilityId,
                        null,
                        RepaymentType.MINIMUM_PAYMENT
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenRepaymentTypeIsNull() {
        assertInvalidCommand(
                new CreateCreditRepaymentMandateCommand(
                        userId,
                        creditFacilityId,
                        accountId,
                        null
                )
        );
    }

    private void assertInvalidCommand(
            CreateCreditRepaymentMandateCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityRepository,
                mandateRepository,
                ownershipPort
        );
    }

    // --------------------------------------------------
    // Credit facility validation
    // --------------------------------------------------

    @Test
    void execute_shouldThrowCreditFacilityNotFound_whenFacilityDoesNotExist() {
        // Arrange
        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.empty());

        // Act
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Assert
        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verifyNoInteractions(
                mandateRepository,
                ownershipPort,
                creditFacility
        );
    }

    @Test
    void execute_shouldThrowAccessDenied_whenUserDoesNotOwnCreditFacility() {
        // Arrange
        UUID anotherUserId = UUID.randomUUID();

        mockExistingCreditFacility();

        when(creditFacility.getCustomerId())
                .thenReturn(anotherUserId);

        // Act
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Assert
        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verifyNoInteractions(
                mandateRepository,
                ownershipPort
        );
    }

    // --------------------------------------------------
    // Account ownership validation
    // --------------------------------------------------

    @Test
    void execute_shouldThrowAccessDenied_whenAccountIsNotOwnedByUser() {
        // Arrange
        mockExistingCreditFacility();
        mockAuthorizedCustomer();

        when(ownershipPort.verify(userId, accountId))
                .thenReturn(false);

        // Act
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Assert
        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verify(ownershipPort)
                .verify(userId, accountId);

        verifyNoInteractions(mandateRepository);
    }

    // --------------------------------------------------
    // Duplicate active mandate
    // --------------------------------------------------

    @Test
    void execute_shouldThrowAlreadyExists_whenActiveMandateAlreadyExists() {
        // Arrange
        mockExistingCreditFacility();
        mockAuthorizedCustomer();
        mockOwnedAccount();

        when(mandateRepository
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        creditFacilityId,
                        accountId,
                        CreditRepaymentMandateStatus.ACTIVE
                ))
                .thenReturn(true);

        // Act
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Assert
        assertEquals(
                ErrorCode.CREDIT_REPAYMENT_MANDATE_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(creditFacilityRepository)
                .findById(creditFacilityId);

        verify(creditFacility)
                .getCustomerId();

        verify(ownershipPort)
                .verify(userId, accountId);

        verify(mandateRepository)
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        creditFacilityId,
                        accountId,
                        CreditRepaymentMandateStatus.ACTIVE
                );

        verify(mandateRepository, never())
                .save(any(CreditRepaymentMandate.class));
    }

    // --------------------------------------------------
    // Verification of processing order
    // --------------------------------------------------

    @Test
    void execute_shouldVerifyOwnershipBeforeCheckingDuplicateMandate() {
        // Arrange
        mockSuccessfulExecution();

        // Act
        handler.execute(command);

        // Assert
        var order = inOrder(
                creditFacilityRepository,
                creditFacility,
                ownershipPort,
                mandateRepository
        );

        order.verify(creditFacilityRepository)
                .findById(creditFacilityId);

        order.verify(creditFacility)
                .getCustomerId();

        order.verify(ownershipPort)
                .verify(userId, accountId);

        order.verify(mandateRepository)
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        creditFacilityId,
                        accountId,
                        CreditRepaymentMandateStatus.ACTIVE
                );
    }

    @Test
    void execute_shouldNotSaveMandate_whenAccountOwnershipVerificationFails() {
        // Arrange
        mockExistingCreditFacility();
        mockAuthorizedCustomer();

        when(ownershipPort.verify(userId, accountId))
                .thenReturn(false);

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(mandateRepository, never())
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        any(),
                        any(),
                        any()
                );

        verify(mandateRepository, never())
                .save(any(CreditRepaymentMandate.class));
    }

    @Test
    void execute_shouldNotSaveMandate_whenFacilityAuthorizationFails() {
        // Arrange
        mockExistingCreditFacility();

        when(creditFacility.getCustomerId())
                .thenReturn(UUID.randomUUID());

        // Act & Assert
        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(ownershipPort);

        verify(mandateRepository, never())
                .existsByCreditFacilityIdAndAccountIdAndStatus(
                        any(),
                        any(),
                        any()
                );

        verify(mandateRepository, never())
                .save(any(CreditRepaymentMandate.class));
    }

    // --------------------------------------------------
    // Command factory
    // --------------------------------------------------

    private CreateCreditRepaymentMandateCommand validCommand() {
        return new CreateCreditRepaymentMandateCommand(
                userId,
                creditFacilityId,
                accountId,
                RepaymentType.MINIMUM_PAYMENT
        );
    }
}