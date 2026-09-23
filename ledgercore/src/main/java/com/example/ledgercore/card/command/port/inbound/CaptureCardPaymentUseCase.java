package com.example.ledgercore.card.command.port.inbound;

import com.example.ledgercore.card.command.dto.CaptureCardPaymentCommand;
import com.example.ledgercore.card.command.dto.CaptureCardPaymentResult;

public interface CaptureCardPaymentUseCase {

    CaptureCardPaymentResult execute(
            CaptureCardPaymentCommand command
    );
}
