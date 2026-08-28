package com.example.ledgercore.notification.mail.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum EmailTemplateType {

    EMAIL_VERIFICATION(
            "email-verification",
            "Verify your email",
            List.of(
                    "otp",
                    "expiresInMinutes"
            )
    ),

    TRANSFER_CONFIRMATION(
            "transfer-confirmation",
            "Confirm your transfer",
            List.of(
                    "otp",
                    "expiresInMinutes",
                    "destinationAccountNo",
                    "amount",
                    "currency"
            )
    );

    private final String templateName;
    private final String subject;
    private final List<String> requiredVariables;

    EmailTemplateType(
            String templateName,
            String subject,
            List<String> requiredVariables
    ) {
        this.templateName = templateName;
        this.subject = subject;
        this.requiredVariables = requiredVariables;
    }

    public String getTemplatePath() {
        return "email/" + templateName;
    }
}