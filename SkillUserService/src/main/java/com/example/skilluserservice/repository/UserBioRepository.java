package com.example.skilluserservice.repository;

import com.example.skilluserservice.entity.UserBio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBioRepository extends JpaRepository<UserBio, Long> {
    Optional<UserBio> findByAuthUserId(Long authUserId);
}