package com.example.skillswapservice.repository; // Və ya uyğun bir paket

import com.example.skillswapservice.entity.GeoSwapOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexCreator implements CommandLineRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(String... args) throws Exception {
        // 1. İndeksin mövcud olub-olmadığını yoxlayın
        if (!elasticsearchOperations.indexOps(GeoSwapOffer.class).exists()) {

            // 2. İndeksi yaradın
            elasticsearchOperations.indexOps(GeoSwapOffer.class).create();

            // 3. Entity-dəki mapping-ləri tətbiq edin
            elasticsearchOperations.indexOps(GeoSwapOffer.class).putMapping();

            System.out.println("Elasticsearch indeksi 'swap_offers' uğurla yaradıldı və mapping tətbiq edildi.");
        }
    }
}