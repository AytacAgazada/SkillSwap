package com.example.skillswapservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * İstifadəçi öz təklifinə qarşılıq tələb etməyə cəhd etdikdə atılır (HTTP 400 - Bad Request).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfMatchException extends RuntimeException {
    public SelfMatchException() {
        super("A user cannot request a match on their own offer.");
    }
}
