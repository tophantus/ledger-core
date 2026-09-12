package com.example.ledgercore.interest.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.interest.query.dto.GetAdminInterestPostingsQuery;
import com.example.ledgercore.interest.query.dto.InterestPostingResponse;

public interface GetAdminInterestPostingsUseCase {

    PageResponse<InterestPostingResponse> execute(
            GetAdminInterestPostingsQuery query
    );
}