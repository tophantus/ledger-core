package com.example.ledgercore.account.command.repository;

import com.example.ledgercore.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountCommandRepository extends JpaRepository<Account, UUID> {
}
