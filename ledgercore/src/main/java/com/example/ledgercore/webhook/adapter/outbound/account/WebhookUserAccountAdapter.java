package com.example.ledgercore.webhook.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.GetAccountIdsByUserUseCase;
import com.example.ledgercore.webhook.query.port.outbound.WebhookUserAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebhookUserAccountAdapter implements WebhookUserAccountPort {

    private final GetAccountIdsByUserUseCase
            getAccountIdsByUserUseCase;

    @Override
    public List<UUID> getAccountIds(UUID userId) {
        return getAccountIdsByUserUseCase.execute(userId);
    }
}