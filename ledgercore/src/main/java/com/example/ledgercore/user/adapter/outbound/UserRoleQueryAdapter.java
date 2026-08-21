package com.example.ledgercore.user.adapter.outbound;

import com.example.ledgercore.role.query.dto.GetUserRoleNamesQuery;
import com.example.ledgercore.role.query.port.inbound.GetUserRoleNamesUseCase;
import com.example.ledgercore.user.query.port.outbound.UserRoleQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRoleQueryAdapter
        implements UserRoleQueryPort {

    private final GetUserRoleNamesUseCase
            getUserRoleNamesUseCase;

    @Override
    public Set<String> getRoleNames(
            UUID userId
    ) {
        return getUserRoleNamesUseCase.execute(
                new GetUserRoleNamesQuery(userId)
        );
    }
}