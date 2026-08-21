package com.example.ledgercore.user.adapter.outbound;

import com.example.ledgercore.role.command.dto.AssignCustomerRoleCommand;
import com.example.ledgercore.role.command.port.inbound.AssignCustomerRoleUseCase;
import com.example.ledgercore.user.command.port.outbound.UserRoleAssignmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRoleAssignmentAdapter
        implements UserRoleAssignmentPort {

    private final AssignCustomerRoleUseCase
            assignCustomerRoleUseCase;

    @Override
    public void assignCustomerRole(
            UUID userId
    ) {
        assignCustomerRoleUseCase.execute(
                new AssignCustomerRoleCommand(userId)
        );
    }
}