package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.CreateCreditRepaymentMandateResult;

public interface CreateCreditRepaymentMandateUseCase {

    CreateCreditRepaymentMandateResult execute(
            CreateCreditRepaymentMandateCommand command
    );
}