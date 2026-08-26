package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserAuthenticationUseCase;
import com.example.ledgercore.user.query.port.outbound.UserRoleQueryPort;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserAuthenticationHandler
        implements GetUserAuthenticationUseCase {

    private final UserQueryRepository userQueryRepository;
    private final UserRoleQueryPort userRoleQueryPort;

    @Override
    public Optional<UserAuthenticationResponse> findByEmail(
            String email
    ) {
        return userQueryRepository
                .findByEmail(email)
                .map(this::toResponse);
    }

    @Override
    public Optional<UserAuthenticationResponse> findById(
            UUID userId
    ) {
        return userQueryRepository
                .findById(userId)
                .map(this::toResponse);
    }

    private UserAuthenticationResponse toResponse(
            User user
    ) {
        Set<String> roles =
                userRoleQueryPort.getRoleNames(
                        user.getId()
                );

        return new UserAuthenticationResponse(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                roles,
                user.isActive()
        );
    }
}