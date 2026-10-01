package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditStatementStatus;

public interface OverdueCreditStatementProcessor {

    CreditStatementStatus getStatus();

    void process(CreditStatement statement);
}