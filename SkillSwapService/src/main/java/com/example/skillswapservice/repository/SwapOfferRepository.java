package com.example.skillswapservice.repository;

import com.example.skillswapservice.entity.SwapOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SwapOfferRepository extends JpaRepository<SwapOfferEntity, Long> {

    List<SwapOfferEntity> findByUserIdAndIsActiveTrue(UUID userId);
}