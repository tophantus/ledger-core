package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.query.dto.UserEmailResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserEmailUseCase;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserEmailHandler
        implements GetUserEmailUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public UserEmailResponse execute(UUID userId) {

        User user = userQueryRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND
                ));

        return new UserEmailResponse(
                user.getEmail()
        );
    }
}
