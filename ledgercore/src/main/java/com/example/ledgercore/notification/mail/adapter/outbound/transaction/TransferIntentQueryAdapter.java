package com.example.ledgercore.notification.mail.adapter.outbound.transaction;

import com.example.ledgercore.notification.mail.command.port.outbound.TransferIntentQueryPort;
import com.example.ledgercore.notification.mail.command.port.outbound.dto.TransferIntentNotificationInfo;
import com.example.ledgercore.transaction.query.port.inbound.GetTransferIntentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferIntentQueryAdapter
        implements TransferIntentQueryPort {

    private final GetTransferIntentUseCase getTransferIntentUseCase;

    @Override
    public TransferIntentNotificationInfo getTransferIntent(
            UUID transferIntentId
    ) {
        com.example.ledgercore.transaction.query.dto.TransferIntentNotificationInfo info =
                getTransferIntentUseCase.execute(
                        transferIntentId
                );

        return new TransferIntentNotificationInfo(
                info.destinationAccountNo(),
                info.amount(),
                info.currency(),
                info.reference(),
                info.description()
        );
    }
}