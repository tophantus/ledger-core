package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.entity.UserProfile;
import com.example.ledgercore.user.query.port.inbound.GetUserFullNameUseCase;
import com.example.ledgercore.user.query.repository.UserProfileQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserFullNameHandler
        implements GetUserFullNameUseCase {

    private final UserProfileQueryRepository userProfileQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public String execute(UUID userId) {

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        UserProfile userProfile =
                userProfileQueryRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_PROFILE_NOT_FOUND
                                )
                        );

        return userProfile.getFullName();
    }
}