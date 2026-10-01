package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.messaging.repayment.RepaymentJob;

public interface RepaymentJobPublisherPort {

    void publish(RepaymentJob job);
}