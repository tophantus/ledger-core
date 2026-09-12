package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.interest.entity.InterestPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InterestPostingQueryRepository
        extends JpaRepository<InterestPosting, UUID>,
        JpaSpecificationExecutor<InterestPosting> {
}