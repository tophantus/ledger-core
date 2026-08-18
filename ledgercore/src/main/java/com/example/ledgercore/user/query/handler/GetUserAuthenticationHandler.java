package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserAuthenticationUseCase;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserAuthenticationHandler
        implements GetUserAuthenticationUseCase {

    private final UserQueryRepository userQueryRepository;

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

    private UserAuthenticationResponse toResponse(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getRole().getName())
                .collect(Collectors.toSet());

        return new UserAuthenticationResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                roles,
                user.isActive()
        );
    }
}