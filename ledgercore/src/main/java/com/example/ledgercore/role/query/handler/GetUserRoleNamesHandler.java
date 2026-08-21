package com.example.ledgercore.role.query.handler;

import com.example.ledgercore.role.query.dto.GetUserRoleNamesQuery;
import com.example.ledgercore.role.query.port.inbound.GetUserRoleNamesUseCase;
import com.example.ledgercore.role.query.repository.UserRoleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetUserRoleNamesHandler
        implements GetUserRoleNamesUseCase {

    private final UserRoleQueryRepository userRoleQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<String> execute(
            GetUserRoleNamesQuery query
    ) {
        return userRoleQueryRepository
                .findRoleNamesByUserId(query.userId());
    }
}