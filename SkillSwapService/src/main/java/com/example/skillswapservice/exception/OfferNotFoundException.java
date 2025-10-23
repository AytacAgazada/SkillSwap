package com.example.skillswapservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Təklif tapılmadıqda atılır (HTTP 404 - Not Found).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class OfferNotFoundException extends RuntimeException {
    public OfferNotFoundException(Long offerId) {
        super("Swap Offer with ID " + offerId + " not found.");
    }
}
