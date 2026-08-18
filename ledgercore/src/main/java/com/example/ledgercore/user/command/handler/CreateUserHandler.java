package com.example.ledgercore.user.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.command.dto.CreateUserCommand;
import com.example.ledgercore.user.command.port.inbound.CreateUserUseCase;
import com.example.ledgercore.user.command.repository.UserCommandRepository;
import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateUserHandler
        implements CreateUserUseCase {

    private final UserCommandRepository userCommandRepository;

    @Override
    @Transactional
    public Optional<UserAuthenticationResponse> execute(CreateUserCommand command) {

        if (userCommandRepository.existsByEmail(command.email())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        User user = User.builder()
                .username(command.username())
                .email(command.email())
                .passwordHash(command.passwordHash())
                .build();

        User savedUser = userCommandRepository
                .save(user);

        return Optional.of(toResponse(savedUser));
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