package com.example.ledgercore.ledger.service;

import org.springframework.stereotype.Service;

@Service
public class LedgerAccountCodeService {

    private static final String CUSTOMER_CODE_PREFIX = "CUSTOMER-";
    private static final String CUSTOMER_NAME_PREFIX = "Customer Account ";

    public String generateCustomerCode(String accountNo) {
        return CUSTOMER_CODE_PREFIX + accountNo;
    }

    public String generateCustomerName(String accountNo) {
        return CUSTOMER_NAME_PREFIX + accountNo;
    }
}