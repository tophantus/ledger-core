package com.example.ledgercore.withdrawal.query.specification;

import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public final class WithdrawalIntentSpecification {

    private WithdrawalIntentSpecification() {
    }

    public static Specification<WithdrawalIntent> accountId(
            UUID accountId
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("accountId"),
                        accountId
                );
    }

    public static Specification<WithdrawalIntent> accountIds(
            List<UUID> accountIds
    ) {
        return (root, query, cb) ->
                root.get("accountId").in(accountIds);
    }

    public static Specification<WithdrawalIntent> status(
            WithdrawalIntentStatus status
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("status"),
                        status
                );
    }
}