package com.example.ledgercore.account.query.service;

import com.example.ledgercore.account.query.service.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;

public interface GetUserAccountService {

    GetUserAccountResult execute(GetUserAccountQuery query);
}