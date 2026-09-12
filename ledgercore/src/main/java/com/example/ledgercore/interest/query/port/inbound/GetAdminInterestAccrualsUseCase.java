package com.example.ledgercore.interest.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.interest.query.dto.GetAdminInterestAccrualsQuery;
import com.example.ledgercore.interest.query.dto.InterestAccrualResponse;

public interface GetAdminInterestAccrualsUseCase {

    PageResponse<InterestAccrualResponse> execute(
            GetAdminInterestAccrualsQuery query
    );
}