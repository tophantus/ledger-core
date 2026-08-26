package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferMoneyHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private AccountTransferPort accountTransferPort;

    @Mock
    private TransferExecutionService transferExecutionService;

    private TransferMoneyHandler handler;

    private UUID userId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        handler = new TransferMoneyHandler(
                transactionCommandRepository,
                accountTransferPort,
                transferExecutionService
        );

        userId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldTransferSuccessfully() {
        TransferMoneyCommand command = command();

        when(transactionCommandRepository.findByReference(command.reference()))
                .thenReturn(Optional.empty());

        when(accountTransferPort.getAccountIdByAccountNo(
                command.destinationAccountNo()
        )).thenReturn(destinationAccountId);

        var expectedResponse = response();

        when(transferExecutionService.execute(
                userId,
                command,
                destinationAccountId
        )).thenReturn(expectedResponse);

        var actual = handler.execute(userId, command);

        assertEquals(expectedResponse, actual);

        verify(accountTransferPort)
                .getAccountIdByAccountNo(
                        command.destinationAccountNo()
                );

        verify(transferExecutionService)
                .execute(
                        userId,
                        command,
                        destinationAccountId
                );
    }

    @Test
    void shouldReturnExistingTransaction() {
        TransferMoneyCommand command = command();

        MoneyTransaction existing = MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .build();

        existing.setId(transactionId);

        when(transactionCommandRepository.findByReference(command.reference()))
                .thenReturn(Optional.of(existing));

        var response = handler.execute(userId, command);

        assertEquals(transactionId, response.id());
        assertEquals(command.reference(), response.reference());
        assertEquals(TransactionType.TRANSFER, response.type());
        assertEquals(TransactionStatus.COMPLETED, response.status());
        assertEquals(sourceAccountId, response.sourceAccountId());
        assertEquals(destinationAccountId, response.destinationAccountId());

        verify(accountTransferPort)
                .verifySourceAccountAccess(
                        userId,
                        sourceAccountId
                );

        verify(accountTransferPort, never())
                .getAccountIdByAccountNo(anyString());

        verifyNoInteractions(transferExecutionService);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        TransferMoneyCommand command = new TransferMoneyCommand(
                sourceAccountId,
                "123456789",
                BigDecimal.ZERO,
                "VND",
                "TRF-001",
                "test"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(accountTransferPort);
        verifyNoInteractions(transferExecutionService);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        TransferMoneyCommand command = new TransferMoneyCommand(
                sourceAccountId,
                "123456789",
                BigDecimal.valueOf(-100),
                "VND",
                "TRF-001",
                "test"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenDestinationIsSameAsSource() {
        TransferMoneyCommand command = command();

        when(transactionCommandRepository.findByReference(command.reference()))
                .thenReturn(Optional.empty());

        when(accountTransferPort.getAccountIdByAccountNo(
                command.destinationAccountNo()
        )).thenReturn(sourceAccountId);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(userId, command)
        );

        assertEquals(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verify(transferExecutionService, never())
                .execute(any(), any(), any());
    }

    private TransferMoneyCommand command() {
        return new TransferMoneyCommand(
                sourceAccountId,
                "987654321",
                BigDecimal.valueOf(100_000),
                "VND",
                "TRF-001",
                "transfer test"
        );
    }

    private com.example.ledgercore.transaction.query.dto.TransactionResponse response() {
        return new com.example.ledgercore.transaction.query.dto.TransactionResponse(
                transactionId,
                "TRF-001",
                TransactionType.TRANSFER,
                TransactionStatus.COMPLETED,
                sourceAccountId,
                destinationAccountId,
                BigDecimal.valueOf(100_000),
                "VND",
                "transfer test",
                null,
                null
        );
    }
}