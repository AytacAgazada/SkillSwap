package com.example.skillswapservice.service;

import com.example.skillswapservice.dto.CreateSwapOfferRequest;
import com.example.skillswapservice.dto.SwapOfferDTO;
import com.example.skillswapservice.entity.GeoSwapOffer;
import com.example.skillswapservice.entity.SwapOfferEntity;
import com.example.skillswapservice.enumeration.SwapStatus;
import com.example.skillswapservice.exception.OfferNotFoundException;
import com.example.skillswapservice.exception.SelfMatchException;
import com.example.skillswapservice.mapper.OfferMapper;
import com.example.skillswapservice.repository.SwapOfferRepository;
import com.example.skillswapservice.repository.SwapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SwapService {

    private final SwapOfferRepository swapOfferRepository;
    private final SwapRepository swapRepository;
    private final SwapEventPublisher eventPublisher;
    private final MatchingService matchingService;
    private final OfferMapper offerMapper;

    @Transactional
    public SwapOfferDTO createOffer(CreateSwapOfferRequest request, UUID userId) {
        log.info("Creating swap offer for user: {}", userId);

        SwapOfferEntity offer = offerMapper.toEntity(request, userId);

        SwapOfferEntity savedOffer = swapOfferRepository.save(offer);
        matchingService.indexOffer(savedOffer);

        log.info("Offer successfully created and indexed. ID: {}", savedOffer.getId());
        return offerMapper.toDto(savedOffer);
    }

    public List<SwapOfferDTO> getUserOffers(UUID userId) {
        List<SwapOfferEntity> entities = swapOfferRepository.findByUserIdAndIsActiveTrue(userId);
        return entities.stream()
                .map(offerMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<SwapOfferDTO> searchOffers(String skill, double lat, double lon, double radiusKm) {
        log.debug("Searching offers for skill: {} within {} km radius.", skill, radiusKm);

        List<Long> offerIds = matchingService.findMatches(skill, lat, lon, radiusKm)
                .stream()
                .map(GeoSwapOffer::getId)
                .collect(Collectors.toList());

        List<SwapOfferEntity> entities = swapOfferRepository.findAllById(offerIds);

        return entities.stream()
                .map(offerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendMatchRequest(Long offerId, UUID requestingUserId) {
        log.info("Match request from user {} for offer {}", requestingUserId, offerId);

        SwapOfferEntity targetOffer = swapOfferRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId));

        if (targetOffer.getUserId().equals(requestingUserId)) {
            log.warn("User attempted to request match on own offer.");
            throw new SelfMatchException();
        }

        eventPublisher.publishMatchRequestedEvent(requestingUserId, targetOffer.getUserId(), offerId);
    }

    @Transactional
    public void completeSwap(Long swapId, UUID user1Id, UUID user2Id) {
        log.info("Completing swap {} between {} and {}", swapId, user1Id, user2Id);

        swapRepository.updateStatus(swapId, SwapStatus.COMPLETED);

        eventPublisher.publishSwapCompletedEvent(swapId, user1Id, user2Id);
    }
}
