package com.example.skillswapservice.repository;

import com.example.skillswapservice.entity.GeoSwapOffer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GeoSwapOfferRepository extends ElasticsearchRepository<GeoSwapOffer, Long> {

    List<GeoSwapOffer> findBySkillRequestedAndIsActiveTrue(String skillRequested);

    List<GeoSwapOffer> findByUserId(UUID userId);
}
