package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.UpdateCreditRepaymentMandateResult;

public interface UpdateCreditRepaymentMandateUseCase {

    UpdateCreditRepaymentMandateResult execute(
            UpdateCreditRepaymentMandateCommand command
    );
}