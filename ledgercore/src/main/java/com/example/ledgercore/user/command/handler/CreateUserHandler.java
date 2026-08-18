package com.example.ledgercore.user.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.command.dto.CreateUserCommand;
import com.example.ledgercore.user.command.port.inbound.CreateUserUseCase;
import com.example.ledgercore.user.command.repository.UserCommandRepository;
import com.example.ledgercore.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateUserHandler
        implements CreateUserUseCase {

    private final UserCommandRepository userCommandRepository;

    @Override
    @Transactional
    public UUID execute(CreateUserCommand command) {

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

        return userCommandRepository
                .save(user)
                .getId();
    }
}