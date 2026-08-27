package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.entity.UserProfile;
import com.example.ledgercore.user.query.dto.CurrentUserResponse;
import com.example.ledgercore.user.query.dto.UserProfileResponse;
import com.example.ledgercore.user.query.port.inbound.GetCurrentUserUseCase;
import com.example.ledgercore.user.query.repository.UserProfileQueryRepository;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentUserHandler
        implements GetCurrentUserUseCase {

    private final UserQueryRepository userQueryRepository;
    private final UserProfileQueryRepository userProfileQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse execute(UUID userId) {

        User user = userQueryRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        UserProfile profile = userProfileQueryRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_PROFILE_NOT_FOUND
                        )
                );

        return toResponse(user, profile);
    }

    private CurrentUserResponse toResponse(
            User user,
            UserProfile profile
    ) {
        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                new UserProfileResponse(
                        profile.getFullName(),
                        profile.getAvatarUrl()
                ),
                user.getCreatedAt()
        );
    }
}