package com.example.ledgercore.user.command.repository;

import com.example.ledgercore.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileCommandRepository extends JpaRepository<UserProfile, UUID> {
}
