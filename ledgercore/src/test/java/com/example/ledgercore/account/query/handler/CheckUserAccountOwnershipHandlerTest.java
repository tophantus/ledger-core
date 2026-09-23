package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.repository.UserAccountQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckUserAccountOwnershipHandlerTest {

    @Mock
    private UserAccountQueryRepository userAccountQueryRepository;

    private CheckUserAccountOwnershipHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new CheckUserAccountOwnershipHandler(
                userAccountQueryRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnTrueWhenUserOwnsAccount() {
        when(userAccountQueryRepository.existsByAccountIdAndUserId(
                accountId,
                userId
        )).thenReturn(true);

        boolean result = handler.execute(
                userId,
                accountId
        );

        assertTrue(result);

        verify(userAccountQueryRepository)
                .existsByAccountIdAndUserId(
                        accountId,
                        userId
                );
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotOwnAccount() {
        when(userAccountQueryRepository.existsByAccountIdAndUserId(
                accountId,
                userId
        )).thenReturn(false);

        boolean result = handler.execute(
                userId,
                accountId
        );

        assertFalse(result);

        verify(userAccountQueryRepository)
                .existsByAccountIdAndUserId(
                        accountId,
                        userId
                );
    }

    @Test
    void shouldNotInteractWithRepositoryWhenUserIdIsNull() {
        boolean result = handler.execute(
                null,
                accountId
        );

        assertFalse(result);

        verifyNoInteractions(
                userAccountQueryRepository
        );
    }

    @Test
    void shouldNotInteractWithRepositoryWhenAccountIdIsNull() {
        boolean result = handler.execute(
                userId,
                null
        );

        assertFalse(result);

        verifyNoInteractions(
                userAccountQueryRepository
        );
    }
}