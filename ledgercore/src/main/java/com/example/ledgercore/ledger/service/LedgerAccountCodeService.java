package com.example.ledgercore.ledger.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LedgerAccountCodeService {

    private static final String CUSTOMER_CODE_PREFIX = "CUSTOMER-";
    private static final String CUSTOMER_NAME_PREFIX = "Customer Account ";

    private static final String CREDIT_CODE_PREFIX = "CREDIT-";
    private static final String CREDIT_NAME_PREFIX = "Credit Facility ";

    public String generateCustomerCode(String accountNo) {
        return CUSTOMER_CODE_PREFIX + accountNo;
    }

    public String generateCustomerName(String accountNo) {
        return CUSTOMER_NAME_PREFIX + accountNo;
    }

    public String generateCreditCode(UUID creditFacilityId) {
        return CREDIT_CODE_PREFIX + creditFacilityId;
    }

    public String generateCreditName(UUID creditFacilityId) {
        return CREDIT_NAME_PREFIX + creditFacilityId;
    }
}