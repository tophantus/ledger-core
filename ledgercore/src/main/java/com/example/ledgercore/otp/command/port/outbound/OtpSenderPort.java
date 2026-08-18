package com.example.ledgercore.otp.command.port.outbound;

import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;

public interface OtpSenderPort {

    void send(
            OtpPurpose purpose,
            OtpChannel channel,
            String destination,
            String otp
    );
}