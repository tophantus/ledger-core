package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.entity.CreditStatement;

public interface OverdueCreditStatementDispatcher {

    void dispatch(CreditStatement statement);
}