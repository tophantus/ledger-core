package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.entity.UserProfile;
import com.example.ledgercore.user.enums.UserStatus;
import com.example.ledgercore.user.query.dto.CurrentUserResponse;
import com.example.ledgercore.user.query.port.outbound.UserRoleQueryPort;
import com.example.ledgercore.user.query.repository.UserProfileQueryRepository;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserHandlerTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private UserProfileQueryRepository userProfileQueryRepository;

    @Mock
    private UserRoleQueryPort userRoleQueryPort;

    private GetCurrentUserHandler handler;

    private UUID userId;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        handler = new GetCurrentUserHandler(
                userQueryRepository,
                userProfileQueryRepository,
                userRoleQueryPort
        );

        userId = UUID.randomUUID();
        createdAt = Instant.now();
    }

    @Test
    void shouldGetCurrentUserSuccessfully() {
        User user = createUser();
        UserProfile profile = createUserProfile();

        Set<String> roles = Set.of("USER");

        when(userQueryRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileQueryRepository.findById(userId))
                .thenReturn(Optional.of(profile));

        when(userRoleQueryPort.getRoleNames(userId))
                .thenReturn(roles);

        CurrentUserResponse response =
                handler.execute(userId);

        assertNotNull(response);

        assertEquals(
                userId,
                response.id()
        );

        assertEquals(
                "user@example.com",
                response.email()
        );

        assertEquals(
                UserStatus.ACTIVE,
                response.status()
        );

        assertEquals(
                roles,
                response.roles()
        );

        assertNotNull(response.profile());

        assertEquals(
                "Tu Phan",
                response.profile().fullName()
        );

        assertEquals(
                "https://example.com/avatar.jpg",
                response.profile().avatarUrl()
        );

        assertEquals(
                createdAt,
                response.createdAt()
        );

        verify(userQueryRepository)
                .findById(userId);

        verify(userProfileQueryRepository)
                .findById(userId);

        verify(userRoleQueryPort)
                .getRoleNames(userId);

        verifyNoMoreInteractions(
                userQueryRepository,
                userProfileQueryRepository,
                userRoleQueryPort
        );
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userQueryRepository.findById(userId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(userId)
                );

        assertEquals(
                "USER_NOT_FOUND",
                exception.getErrorCode().name()
        );

        verify(userQueryRepository)
                .findById(userId);

        verifyNoInteractions(
                userProfileQueryRepository,
                userRoleQueryPort
        );
    }

    @Test
    void shouldThrowWhenUserProfileNotFound() {
        User user = createUser();

        when(userQueryRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileQueryRepository.findById(userId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(userId)
                );

        assertEquals(
                "USER_PROFILE_NOT_FOUND",
                exception.getErrorCode().name()
        );

        verify(userQueryRepository)
                .findById(userId);

        verify(userProfileQueryRepository)
                .findById(userId);

        verifyNoInteractions(
                userRoleQueryPort
        );
    }

    @Test
    void shouldReturnProfileWithoutAvatar() {
        User user = createUser();

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .fullName("Tu Phan")
                .avatarUrl(null)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        Set<String> roles = Set.of("USER");

        when(userQueryRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileQueryRepository.findById(userId))
                .thenReturn(Optional.of(profile));

        when(userRoleQueryPort.getRoleNames(userId))
                .thenReturn(roles);

        CurrentUserResponse response =
                handler.execute(userId);

        assertNotNull(response);

        assertEquals(
                "Tu Phan",
                response.profile().fullName()
        );

        assertNull(
                response.profile().avatarUrl()
        );

        assertEquals(
                roles,
                response.roles()
        );

        verify(userQueryRepository)
                .findById(userId);

        verify(userProfileQueryRepository)
                .findById(userId);

        verify(userRoleQueryPort)
                .getRoleNames(userId);

        verifyNoMoreInteractions(
                userQueryRepository,
                userProfileQueryRepository,
                userRoleQueryPort
        );
    }

    @Test
    void shouldReturnMultipleRoles() {
        User user = createUser();
        UserProfile profile = createUserProfile();

        Set<String> roles = Set.of(
                "USER",
                "ADMIN"
        );

        when(userQueryRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileQueryRepository.findById(userId))
                .thenReturn(Optional.of(profile));

        when(userRoleQueryPort.getRoleNames(userId))
                .thenReturn(roles);

        CurrentUserResponse response =
                handler.execute(userId);

        assertEquals(
                roles,
                response.roles()
        );

        verify(userRoleQueryPort)
                .getRoleNames(userId);
    }

    @Test
    void shouldReturnEmptyRoles() {
        User user = createUser();
        UserProfile profile = createUserProfile();

        when(userQueryRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userProfileQueryRepository.findById(userId))
                .thenReturn(Optional.of(profile));

        when(userRoleQueryPort.getRoleNames(userId))
                .thenReturn(Set.of());

        CurrentUserResponse response =
                handler.execute(userId);

        assertNotNull(response);

        assertTrue(
                response.roles().isEmpty()
        );

        verify(userRoleQueryPort)
                .getRoleNames(userId);
    }

    private User createUser() {
        return User.builder()
                .id(userId)
                .email("user@example.com")
                .passwordHash("hashed-password")
                .status(UserStatus.ACTIVE)
                .verifiedAt(createdAt)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private UserProfile createUserProfile() {
        return UserProfile.builder()
                .userId(userId)
                .fullName("Tu Phan")
                .avatarUrl(
                        "https://example.com/avatar.jpg"
                )
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}