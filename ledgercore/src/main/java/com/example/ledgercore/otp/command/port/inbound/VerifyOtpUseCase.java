package com.example.ledgercore.otp.command.port.inbound;

import com.example.ledgercore.otp.command.dto.VerifyOtpCommand;
import com.example.ledgercore.otp.enums.OtpStatus;

public interface VerifyOtpUseCase {

    OtpStatus execute(VerifyOtpCommand command);
}