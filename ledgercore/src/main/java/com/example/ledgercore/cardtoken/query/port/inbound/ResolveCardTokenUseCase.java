package com.example.ledgercore.cardtoken.query.port.inbound;

import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenQuery;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenResult;

public interface ResolveCardTokenUseCase {

    ResolveCardTokenResult execute(
            ResolveCardTokenQuery query
    );
}