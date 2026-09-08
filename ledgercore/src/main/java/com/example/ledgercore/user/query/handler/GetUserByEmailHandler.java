package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.entity.UserProfile;
import com.example.ledgercore.user.query.dto.UserNotificationResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserByEmailUseCase;
import com.example.ledgercore.user.query.repository.UserProfileQueryRepository;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserByEmailHandler
        implements GetUserByEmailUseCase {

    private final UserQueryRepository userQueryRepository;
    private final UserProfileQueryRepository userProfileQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public UserNotificationResponse execute(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "email must not be blank"
            );
        }

        User user =
                userQueryRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        UserProfile profile =
                userProfileQueryRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_PROFILE_NOT_FOUND
                                )
                        );

        return new UserNotificationResponse(
                profile.getFullName()
        );
    }
}