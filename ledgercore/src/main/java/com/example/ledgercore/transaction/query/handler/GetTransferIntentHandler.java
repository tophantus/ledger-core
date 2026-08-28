package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.query.dto.TransferIntentNotificationInfo;
import com.example.ledgercore.transaction.query.port.inbound.GetTransferIntentUseCase;
import com.example.ledgercore.transaction.query.port.outbound.AccountQueryPort;
import com.example.ledgercore.transaction.query.repository.TransferIntentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTransferIntentHandler
        implements GetTransferIntentUseCase {

    private final TransferIntentQueryRepository
            transferIntentQueryRepository;

    private final AccountQueryPort accountQueryPort;

    @Override
    @Transactional(readOnly = true)
    public TransferIntentNotificationInfo execute(
            UUID transferIntentId
    ) {
        TransferIntent intent =
                transferIntentQueryRepository
                        .findById(transferIntentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TRANSFER_INTENT_NOT_FOUND
                                )
                        );

        String destinationAccountNo =
                accountQueryPort.getAccountNoByAccountId(
                        intent.getDestinationAccountId()
                );

        return new TransferIntentNotificationInfo(
                destinationAccountNo,
                intent.getAmount(),
                intent.getCurrency(),
                intent.getReference(),
                intent.getDescription()
        );
    }
}