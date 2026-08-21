package com.example.ledgercore.role.command.port.inbound;

import com.example.ledgercore.role.command.dto.AssignCustomerRoleCommand;

public interface AssignCustomerRoleUseCase {

    void execute(AssignCustomerRoleCommand command);
}