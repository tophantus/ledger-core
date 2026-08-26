package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyAccountOwnershipHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private VerifyAccountOwnershipHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new VerifyAccountOwnershipHandler(
                accountQueryRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldPassWhenUserOwnsAccount() {
        when(accountQueryRepository
                .existsByIdAndUserId(
                        accountId,
                        userId
                ))
                .thenReturn(true);

        handler.execute(
                userId,
                accountId
        );

        verify(accountQueryRepository)
                .existsByIdAndUserId(
                        accountId,
                        userId
                );

        verifyNoMoreInteractions(accountQueryRepository);
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnAccount() {
        when(accountQueryRepository
                .existsByIdAndUserId(
                        accountId,
                        userId
                ))
                .thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                accountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .existsByIdAndUserId(
                        accountId,
                        userId
                );

        verifyNoMoreInteractions(accountQueryRepository);
    }
}