package com.example.ledgercore.user.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.command.dto.ActivateUserCommand;
import com.example.ledgercore.user.command.port.inbound.ActivateUserUseCase;
import com.example.ledgercore.user.command.repository.UserCommandRepository;
import com.example.ledgercore.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateUserHandler
        implements ActivateUserUseCase {

    private final UserCommandRepository userCommandRepository;

    @Override
    @Transactional
    public void execute(ActivateUserCommand command) {

        User user = userCommandRepository
                .findById(command.userId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        user.activate();
    }
}