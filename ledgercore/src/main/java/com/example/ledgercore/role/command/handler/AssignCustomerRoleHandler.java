package com.example.ledgercore.role.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.role.command.dto.AssignCustomerRoleCommand;
import com.example.ledgercore.role.command.port.inbound.AssignCustomerRoleUseCase;
import com.example.ledgercore.role.command.repository.RoleCommandRepository;
import com.example.ledgercore.role.command.repository.UserRoleCommandRepository;
import com.example.ledgercore.role.entity.Role;
import com.example.ledgercore.role.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignCustomerRoleHandler
        implements AssignCustomerRoleUseCase {

    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final RoleCommandRepository roleCommandRepository;
    private final UserRoleCommandRepository userRoleCommandRepository;

    @Override
    @Transactional
    public void execute(
            AssignCustomerRoleCommand command
    ) {
        Role customerRole =
                roleCommandRepository
                        .findByName(CUSTOMER_ROLE)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ROLE_NOT_FOUND
                                )
                        );

        if (userRoleCommandRepository.existsByUserIdAndRoleId(
                command.userId(),
                customerRole.getId()
        )) {
            return;
        }

        UserRole userRole = UserRole.builder()
                .userId(command.userId())
                .role(customerRole)
                .build();

        userRoleCommandRepository.save(userRole);
    }
}