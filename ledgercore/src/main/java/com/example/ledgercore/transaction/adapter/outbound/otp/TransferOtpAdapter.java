package com.example.ledgercore.transaction.adapter.outbound.otp;

import com.example.ledgercore.otp.command.dto.SendOtpCommand;
import com.example.ledgercore.otp.command.port.inbound.SendOtpUseCase;
import com.example.ledgercore.otp.enums.OtpChannel;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.user.query.dto.UserEmailResponse;
import com.example.ledgercore.user.query.port.inbound.GetUserEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferOtpAdapter
        implements TransferOtpPort {

    private final SendOtpUseCase sendOtpUseCase;
    private final GetUserEmailUseCase getUserEmailUseCase;

    @Override
    public void sendConfirmationOtp(
            UUID userId,
            UUID transferIntentId
    ) {
        UserEmailResponse userEmail =
                getUserEmailUseCase.execute(userId);

        sendOtpUseCase.execute(
                new SendOtpCommand(
                        userId,
                        transferIntentId,
                        OtpPurpose.CONFIRM_TRANSFER,
                        OtpChannel.EMAIL,
                        userEmail.email()
                )
        );
    }
}
