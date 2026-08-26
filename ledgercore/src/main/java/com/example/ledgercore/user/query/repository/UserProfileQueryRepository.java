package com.example.ledgercore.user.query.repository;

import com.example.ledgercore.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileQueryRepository
        extends JpaRepository<UserProfile, UUID> {
}