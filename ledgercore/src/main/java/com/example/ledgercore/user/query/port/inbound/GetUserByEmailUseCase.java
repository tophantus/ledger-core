package com.example.ledgercore.user.query.port.inbound;

import com.example.ledgercore.user.query.dto.UserNotificationResponse;

public interface GetUserByEmailUseCase {

    UserNotificationResponse execute(String email);
}
