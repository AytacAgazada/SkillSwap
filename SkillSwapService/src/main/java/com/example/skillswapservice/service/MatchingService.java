package com.example.skillswapservice.service;

import com.example.skillswapservice.entity.GeoSwapOffer;
import com.example.skillswapservice.entity.SwapOfferEntity;
import com.example.skillswapservice.mapper.OfferMapper;
import com.example.skillswapservice.repository.GeoSwapOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final GeoSwapOfferRepository geoSwapOfferRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final OfferMapper offerMapper; // Mapper əlavə edildi

    /**
     * JPA Entity-ni alıb, onu Geo Entity-yə çevirib Elasticsearch-də indeksləyir.
     */
    public void indexOffer(SwapOfferEntity offer) {
        GeoSwapOffer geoOffer = offerMapper.toGeoEntity(offer); // Mapper istifadəsi
        geoSwapOfferRepository.save(geoOffer);
    }

    // ... findMatches metodu olduğu kimi qalır
    public List<GeoSwapOffer> findMatches(String skillRequested, double lat, double lon, double radiusKm) {
        Criteria criteria = new Criteria("skillRequested").is(skillRequested)
                .and(new Criteria("location").within(new GeoPoint(lat, lon), String.format("%fkm", radiusKm)))
                .and(new Criteria("isActive").is(true));

        Query query = new CriteriaQuery(criteria);
        SearchHits<GeoSwapOffer> searchHits = elasticsearchOperations.search(query, GeoSwapOffer.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}