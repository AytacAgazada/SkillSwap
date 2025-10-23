package com.example.skillswapservice.controller;

import com.example.skillswapservice.dto.CreateSwapOfferRequest;
import com.example.skillswapservice.dto.SwapOfferDTO;
import com.example.skillswapservice.service.SwapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/swaps")
@RequiredArgsConstructor
public class SwapController {

    private final SwapService swapService;

    @PostMapping("/offers")
    public ResponseEntity<SwapOfferDTO> createOffer(
                                                     @RequestHeader("X-Auth-User-Id") UUID userId,
                                                     @Valid @RequestBody CreateSwapOfferRequest request) {

        SwapOfferDTO newOffer = swapService.createOffer(request, userId);
        return new ResponseEntity<>(newOffer, HttpStatus.CREATED);
    }

    @GetMapping("/offers/me")
    public ResponseEntity<List<SwapOfferDTO>> getMyOffers(
                                                           @RequestHeader("X-Auth-User-Id") UUID userId) {

        List<SwapOfferDTO> offers = swapService.getUserOffers(userId);
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/offers/search")
    public ResponseEntity<List<SwapOfferDTO>> searchOffers(
                                                            @RequestParam String skill,
                                                            @RequestParam double lat,
                                                            @RequestParam double lon,
                                                            @RequestParam(defaultValue = "10") double radiusKm) {

        List<SwapOfferDTO> offers = swapService.searchOffers(skill, lat, lon, radiusKm);
        return ResponseEntity.ok(offers);
    }

    @PostMapping("/match/{offerId}")
    public ResponseEntity<String> requestMatch(
            @RequestHeader("X-Auth-User-Id") UUID requestingUserId,
            @PathVariable Long offerId) {

        swapService.sendMatchRequest(offerId, requestingUserId);
        return ResponseEntity.ok("Match request successfully sent. Event published.");
    }

    @PostMapping("/complete/{swapId}")
    public ResponseEntity<String> completeSwap(
            @RequestHeader("X-Auth-User-Id") UUID currentUserId,
            @PathVariable Long swapId,
            @RequestParam UUID otherUserId) {

        swapService.completeSwap(swapId, currentUserId, otherUserId);
        return ResponseEntity.ok("Swap successfully marked as complete. Event published.");
    }
}