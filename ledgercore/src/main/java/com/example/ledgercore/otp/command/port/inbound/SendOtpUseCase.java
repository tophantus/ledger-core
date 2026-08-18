package com.example.ledgercore.otp.command.port.inbound;

import com.example.ledgercore.otp.command.dto.SendOtpCommand;

public interface SendOtpUseCase {

    void execute(SendOtpCommand command);
}