package com.example.ledgercore.account.query.repository;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountQueryRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNo(String accountNo);

    List<Account> findAllByUserIdAndStatusNot(
            UUID userId,
            AccountStatus status
    );
}
