package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckAccountOwnershipHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private CheckAccountOwnershipHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new CheckAccountOwnershipHandler(
                accountQueryRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnTrueWhenUserOwnsAccount() {

        when(accountQueryRepository.existsByIdAndUserId(
                accountId,
                userId
        )).thenReturn(true);

        boolean result = handler.execute(
                userId,
                accountId
        );

        assertTrue(result);

        verify(accountQueryRepository)
                .existsByIdAndUserId(
                        accountId,
                        userId
                );

        verifyNoMoreInteractions(
                accountQueryRepository
        );
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotOwnAccount() {

        when(accountQueryRepository.existsByIdAndUserId(
                accountId,
                userId
        )).thenReturn(false);

        boolean result = handler.execute(
                userId,
                accountId
        );

        assertFalse(result);

        verify(accountQueryRepository)
                .existsByIdAndUserId(
                        accountId,
                        userId
                );

        verifyNoMoreInteractions(
                accountQueryRepository
        );
    }
}