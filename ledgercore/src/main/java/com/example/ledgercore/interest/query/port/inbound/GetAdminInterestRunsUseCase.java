package com.example.ledgercore.interest.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.interest.query.dto.GetAdminInterestRunsQuery;
import com.example.ledgercore.interest.query.dto.InterestRunResponse;

public interface GetAdminInterestRunsUseCase {

    PageResponse<InterestRunResponse> execute(
            GetAdminInterestRunsQuery query
    );
}