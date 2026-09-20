package com.example.ledgercore.user.query.handler;

import com.example.ledgercore.user.entity.User;
import com.example.ledgercore.user.query.dto.GetUserBatchQuery;
import com.example.ledgercore.user.query.dto.GetUserBatchResult;
import com.example.ledgercore.user.query.port.inbound.GetUserBatchUseCase;
import com.example.ledgercore.user.query.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserBatchHandler
        implements GetUserBatchUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public GetUserBatchResult execute(
            GetUserBatchQuery query
    ) {
        return new GetUserBatchResult(
                userQueryRepository
                        .findBatch(
                                query.lastProcessedId(),
                                PageRequest.of(
                                        0,
                                        query.batchSize()
                                )
                        )
                        .stream()
                        .map(this::toUserInfo)
                        .toList()
        );
    }

    private GetUserBatchResult.UserInfo toUserInfo(
            User user
    ) {
        return new GetUserBatchResult.UserInfo(
                user.getId()
        );
    }
}