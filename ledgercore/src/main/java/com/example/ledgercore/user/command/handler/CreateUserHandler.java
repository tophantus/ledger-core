package com.example.ledgercore.user.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.command.dto.CreateUserCommand;
import com.example.ledgercore.user.command.port.inbound.CreateUserUseCase;
import com.example.ledgercore.user.command.port.outbound.UserRoleAssignmentPort;
import com.example.ledgercore.user.command.repository.UserCommandRepository;
import com.example.ledgercore.user.command.repository.UserProfileCommandRepository;
import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.entity.UserProfile;
import com.example.ledgercore.user.query.dto.UserAuthenticationResponse;
import com.example.ledgercore.user.query.port.outbound.UserRoleQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateUserHandler
        implements CreateUserUseCase {

    private final UserCommandRepository userCommandRepository;
    private final UserRoleAssignmentPort userRoleAssignmentPort;
    private final UserRoleQueryPort userRoleQueryPort;

    private final UserProfileCommandRepository userProfileCommandRepository;

    @Override
    @Transactional
    public Optional<UserAuthenticationResponse> execute(
            CreateUserCommand command
    ) {
        if (userCommandRepository.existsByEmail(command.email())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        User user = User.builder()
                .email(command.email())
                .passwordHash(command.passwordHash())
                .build();

        User savedUser =
                userCommandRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .userId(savedUser.getId())
                .fullName(command.fullName())
                .build();

        userProfileCommandRepository.save(profile);

        userRoleAssignmentPort.assignCustomerRole(
                savedUser.getId()
        );

        return Optional.of(
                toResponse(savedUser)
        );
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