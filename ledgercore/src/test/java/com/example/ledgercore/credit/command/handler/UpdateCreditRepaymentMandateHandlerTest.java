
package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateResult;
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
class UpdateCreditRepaymentMandateHandlerTest {

    @Mock
    private CreditRepaymentMandateCommandRepository mandateRepository;

    @Mock
    private CreditFacilityCommandRepository creditFacilityRepository;

    @Mock
    private VerifyRepaymentAccountOwnershipPort ownershipPort;

    @Mock
    private CreditRepaymentMandate mandate;

    @Mock
    private CreditFacility creditFacility;

    @InjectMocks
    private UpdateCreditRepaymentMandateHandler handler;

    private UUID userId;
    private UUID anotherUserId;
    private UUID mandateId;
    private UUID creditFacilityId;
    private UUID accountId;
    private UUID newAccountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        mandateId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        newAccountId = UUID.randomUUID();
    }

    private UpdateCreditRepaymentMandateCommand validCommand() {
        return new UpdateCreditRepaymentMandateCommand(
                userId,
                mandateId,
                accountId,
                RepaymentType.MINIMUM_PAYMENT
        );
    }

    /**
     * Stub các thông tin mandate cần thiết để đi qua bước tìm mandate.
     */
    private void givenMandateExists() {
        when(mandateRepository.findById(mandateId))
                .thenReturn(Optional.of(mandate));
        when(mandate.getCreditFacilityId())
                .thenReturn(creditFacilityId);
    }

    /**
     * Stub facility tồn tại và thuộc về user hiện tại.
     */
    private void givenFacilityExistsAndOwnedByUser() {
        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(creditFacility));
        when(creditFacility.getCustomerId())
                .thenReturn(userId);
    }

    /**
     * Thiết lập mandate ACTIVE và facility thuộc user.
     * Chỉ dùng trong các test cần đi qua bước kiểm tra trạng thái.
     */
    private void givenActiveMandateAndOwnedFacility() {
        givenMandateExists();
        givenFacilityExistsAndOwnedByUser();

        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.ACTIVE);
        when(mandate.getAccountId())
                .thenReturn(accountId);
    }

    private void givenSuccessfulUpdateResult(
            UUID resultAccountId
    ) {
        when(mandate.getId()).thenReturn(mandateId);
        when(mandate.getAccountId()).thenReturn(resultAccountId);
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.MINIMUM_PAYMENT);
        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.ACTIVE);
        when(mandate.getUpdatedAt())
                .thenReturn(Instant.now());
    }

    @Test
    void execute_shouldUpdateMandate_whenCommandIsValid() {
        UpdateCreditRepaymentMandateCommand command = validCommand();

        givenActiveMandateAndOwnedFacility();
        givenSuccessfulUpdateResult(accountId);

        UpdateCreditRepaymentMandateResult result =
                handler.execute(command);

        verify(mandate).update(
                accountId,
                RepaymentType.MINIMUM_PAYMENT
        );
        verify(mandateRepository).save(mandate);
        verifyNoInteractions(ownershipPort);

        assertNotNull(result);
        assertEquals(mandateId, result.mandateId());
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(accountId, result.accountId());
        assertEquals(
                RepaymentType.MINIMUM_PAYMENT,
                result.repaymentType()
        );
        assertEquals(
                CreditRepaymentMandateStatus.ACTIVE,
                result.status()
        );
        assertNotNull(result.updatedAt());
    }

    @Test
    void execute_shouldUpdateRepaymentTypeOnly_whenAccountIdIsNull() {
        UpdateCreditRepaymentMandateCommand command =
                new UpdateCreditRepaymentMandateCommand(
                        userId,
                        mandateId,
                        null,
                        RepaymentType.MINIMUM_PAYMENT
                );

        givenActiveMandateAndOwnedFacility();
        givenSuccessfulUpdateResult(accountId);

        UpdateCreditRepaymentMandateResult result =
                handler.execute(command);

        verify(mandate).update(
                null,
                RepaymentType.MINIMUM_PAYMENT
        );
        verify(mandateRepository).save(mandate);
        verifyNoInteractions(ownershipPort);

        assertEquals(accountId, result.accountId());
    }

    @Test
    void execute_shouldVerifyOwnership_whenChangingAccount() {
        UUID currentAccountId = UUID.randomUUID();
        UUID targetAccountId = UUID.randomUUID();

        assertNotEquals(currentAccountId, targetAccountId);

        UpdateCreditRepaymentMandateCommand command =
                new UpdateCreditRepaymentMandateCommand(
                        userId,
                        mandateId,
                        targetAccountId,
                        RepaymentType.MINIMUM_PAYMENT
                );

        // Arrange: mandate tồn tại.
        when(mandateRepository.findById(mandateId))
                .thenReturn(Optional.of(mandate));
        when(mandate.getCreditFacilityId())
                .thenReturn(creditFacilityId);

        // Arrange: facility tồn tại và thuộc user.
        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(creditFacility));
        when(creditFacility.getCustomerId())
                .thenReturn(userId);

        // Mandate đang ACTIVE.
        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.ACTIVE);

        // Lần đầu: handler đọc tài khoản hiện tại để so sánh.
        // Lần tiếp theo: handler đọc tài khoản sau khi update để tạo result.
        when(mandate.getAccountId())
                .thenReturn(currentAccountId, targetAccountId);

        // Tài khoản mới thuộc quyền sở hữu của user.
        when(ownershipPort.verify(userId, targetAccountId))
                .thenReturn(true);

        // Các thông tin còn lại dùng để tạo result.
        when(mandate.getId())
                .thenReturn(mandateId);
        when(mandate.getRepaymentType())
                .thenReturn(RepaymentType.MINIMUM_PAYMENT);
        when(mandate.getUpdatedAt())
                .thenReturn(Instant.now());

        // Act
        UpdateCreditRepaymentMandateResult result =
                handler.execute(command);

        // Assert: xác minh quyền sở hữu tài khoản mới.
        verify(ownershipPort).verify(userId, targetAccountId);

        // Assert: mandate được cập nhật và lưu.
        verify(mandate).update(
                targetAccountId,
                RepaymentType.MINIMUM_PAYMENT
        );
        verify(mandateRepository).save(mandate);

        // Assert: kết quả phản ánh tài khoản mới.
        assertNotNull(result);
        assertEquals(mandateId, result.mandateId());
        assertEquals(creditFacilityId, result.creditFacilityId());
        assertEquals(targetAccountId, result.accountId());
        assertEquals(
                RepaymentType.MINIMUM_PAYMENT,
                result.repaymentType()
        );
        assertEquals(
                CreditRepaymentMandateStatus.ACTIVE,
                result.status()
        );
        assertNotNull(result.updatedAt());
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );
        verifyNoInteractions(
                mandateRepository,
                creditFacilityRepository,
                ownershipPort
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenUserIdIsNull() {
        UpdateCreditRepaymentMandateCommand command =
                new UpdateCreditRepaymentMandateCommand(
                        null,
                        mandateId,
                        accountId,
                        RepaymentType.MINIMUM_PAYMENT
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );
        verifyNoInteractions(
                mandateRepository,
                creditFacilityRepository,
                ownershipPort
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenMandateIdIsNull() {
        UpdateCreditRepaymentMandateCommand command =
                new UpdateCreditRepaymentMandateCommand(
                        userId,
                        null,
                        accountId,
                        RepaymentType.MINIMUM_PAYMENT
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );
        verifyNoInteractions(
                mandateRepository,
                creditFacilityRepository,
                ownershipPort
        );
    }

    @Test
    void execute_shouldThrowMandateNotFound_whenMandateDoesNotExist() {
        UpdateCreditRepaymentMandateCommand command = validCommand();

        when(mandateRepository.findById(mandateId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_REPAYMENT_MANDATE_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(mandateRepository).findById(mandateId);
        verifyNoInteractions(
                creditFacilityRepository,
                ownershipPort
        );
        verify(mandateRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowFacilityNotFound_whenFacilityDoesNotExist() {
        UpdateCreditRepaymentMandateCommand command = validCommand();

        givenMandateExists();
        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(mandateRepository).findById(mandateId);
        verify(creditFacilityRepository).findById(creditFacilityId);
        verifyNoInteractions(ownershipPort);
        verify(mandateRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowAccessDenied_whenUserDoesNotOwnFacility() {
        UpdateCreditRepaymentMandateCommand command = validCommand();

        // Chỉ stub những gì handler đọc trước khi từ chối quyền truy cập.
        givenMandateExists();
        when(creditFacilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(creditFacility));
        when(creditFacility.getCustomerId())
                .thenReturn(anotherUserId);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(mandateRepository).findById(mandateId);
        verify(creditFacilityRepository).findById(creditFacilityId);
        verifyNoInteractions(ownershipPort);
        verify(mandate, never()).update(any(), any());
        verify(mandateRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowMandateNotActive_whenMandateIsNotActive() {
        UpdateCreditRepaymentMandateCommand command = validCommand();

        givenMandateExists();
        givenFacilityExistsAndOwnedByUser();
        when(mandate.getStatus())
                .thenReturn(CreditRepaymentMandateStatus.REVOKED);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_REPAYMENT_MANDATE_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(mandate).getStatus();
        verifyNoInteractions(ownershipPort);
        verify(mandate, never()).update(any(), any());
        verify(mandateRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowAccessDenied_whenNewAccountIsNotOwned() {
        UpdateCreditRepaymentMandateCommand command =
                new UpdateCreditRepaymentMandateCommand(
                        userId,
                        mandateId,
                        newAccountId,
                        RepaymentType.MINIMUM_PAYMENT
                );

        givenActiveMandateAndOwnedFacility();
        when(ownershipPort.verify(userId, newAccountId))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(ownershipPort).verify(userId, newAccountId);
        verify(mandate, never()).update(any(), any());
        verify(mandateRepository, never()).save(any());
    }

    @Test
    void execute_shouldNotVerifyOwnership_whenAccountIdIsUnchanged() {
        UpdateCreditRepaymentMandateCommand command = validCommand();

        givenActiveMandateAndOwnedFacility();
        givenSuccessfulUpdateResult(accountId);

        UpdateCreditRepaymentMandateResult result =
                handler.execute(command);

        verifyNoInteractions(ownershipPort);
        verify(mandate).update(
                accountId,
                RepaymentType.MINIMUM_PAYMENT
        );
        verify(mandateRepository).save(mandate);

        assertEquals(accountId, result.accountId());
    }
}