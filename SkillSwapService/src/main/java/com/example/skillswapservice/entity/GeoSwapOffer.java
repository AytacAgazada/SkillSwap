package com.example.skillswapservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.UUID;

@Document(indexName = "swap_offers")
@Data
public class GeoSwapOffer {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private UUID userId;

    @Field(type = FieldType.Keyword)
    private String skillOffered;

    @Field(type = FieldType.Keyword)
    private String skillRequested;

    @Field(type = FieldType.Object)
    private GeoPoint location;

    @Field(type = FieldType.Boolean)
    private boolean isActive;
}