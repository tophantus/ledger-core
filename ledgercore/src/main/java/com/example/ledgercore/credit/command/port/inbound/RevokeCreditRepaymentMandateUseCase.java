package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateCommand;
import com.example.ledgercore.credit.command.dto.RevokeCreditRepaymentMandateResult;

public interface RevokeCreditRepaymentMandateUseCase {

    RevokeCreditRepaymentMandateResult execute(
            RevokeCreditRepaymentMandateCommand command
    );
}