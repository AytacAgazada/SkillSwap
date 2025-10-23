package com.example.skillswapservice.mapper;

import com.example.skillswapservice.dto.CreateSwapOfferRequest;
import com.example.skillswapservice.dto.SwapOfferDTO;
import com.example.skillswapservice.entity.GeoSwapOffer;
import com.example.skillswapservice.entity.SwapOfferEntity;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

/**
 * Request DTO, JPA Entity və Geo Indexing Entity arasında konvertasiya üçün istifadə olunur.
 */
@Component
public class OfferMapper {

    /**
     * Request DTO-dan yeni JPA Entity yaradır.
     */
    public SwapOfferEntity toEntity(CreateSwapOfferRequest request, java.util.UUID userId) {
        SwapOfferEntity entity = new SwapOfferEntity();
        entity.setUserId(userId);
        entity.setSkillOffered(request.getSkillOffered());
        entity.setSkillRequested(request.getSkillRequested());
        entity.setMeetingType(request.getMeetingType());
        entity.setDescription(request.getDescription());
        entity.setLatitude(request.getLatitude());
        entity.setLongitude(request.getLongitude());
        entity.setActive(true); // Sahə adı isActive olduğu üçün setIsActive() istifadə olunur.
        return entity;
    }

    /**
     * JPA Entity-dən xarici istifadə üçün DTO-ya konvertasiya edir.
     */
    public SwapOfferDTO toDto(SwapOfferEntity entity) {
        // DTO-da @AllArgsConstructor olduğu üçün indi bu işləyəcək
        return new SwapOfferDTO(
                entity.getId(),
                entity.getUserId(),
                entity.getSkillOffered(),
                entity.getSkillRequested(),
                entity.getMeetingType(),
                entity.getDescription(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }

    /**
     * JPA Entity-dən Elasticsearch-də indeksləmə üçün Geo Entity yaradır.
     */
    public GeoSwapOffer toGeoEntity(SwapOfferEntity entity) {
        GeoSwapOffer geoOffer = new GeoSwapOffer();
        geoOffer.setId(entity.getId());
        geoOffer.setUserId(entity.getUserId());
        geoOffer.setSkillOffered(entity.getSkillOffered());
        geoOffer.setSkillRequested(entity.getSkillRequested());

        // Düzəliş: Primitiv 'boolean' üçün Lombok 'getIsActive()' əvəzinə 'isActive()' yaradır.
        geoOffer.setActive(entity.isActive());

        // GeoPoint istifadəsi
        if (entity.getLatitude() != null && entity.getLongitude() != null) {
            geoOffer.setLocation(new GeoPoint(entity.getLatitude(), entity.getLongitude()));
        }
        return geoOffer;
    }
}
