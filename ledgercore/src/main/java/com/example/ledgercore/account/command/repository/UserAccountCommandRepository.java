package com.example.ledgercore.account.command.repository;

import com.example.ledgercore.account.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountCommandRepository 
        extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByAccountId(UUID accountId);

}
