package com.example.ledgercore.role.query.port.inbound;

import com.example.ledgercore.role.query.dto.GetUserRoleNamesQuery;

import java.util.Set;

public interface GetUserRoleNamesUseCase {

    Set<String> execute(GetUserRoleNamesQuery query);
}