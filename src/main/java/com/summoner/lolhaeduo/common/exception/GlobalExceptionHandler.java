package com.summoner.lolhaeduo.common.exception;

import com.summoner.lolhaeduo.common.exception.riot.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalRateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleInternalRateLimit(InternalRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "message", ex.getMessage(),
                "code", "INTERNAL_RATE_LIMIT",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(RiotApiException.class)
    public ResponseEntity<Map<String, Object>> handleRiotApi(RiotApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "message", ex.getMessage(),
                "code", "RIOT_API_ERROR",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(RiotApiRateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRiotApiRateLimit(RiotApiRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "message", ex.getMessage(),
                "code", "RIOT_API_RATE_LIMIT",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(RiotApiInterruptedException.class)
    public ResponseEntity<Map<String, Object>> handleRiotApiInterrupted(RiotApiInterruptedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message", ex.getMessage(),
                "code", "RIOT_API_INTERRUPTED",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(RiotApiSummonerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRiotApiSummonerNotFound(RiotApiSummonerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", ex.getMessage(),
                "code", "RIOT_API_SUMMONER_NOT_FOUND",
                "timestamp", Instant.now().toString()
        ));
    }

}
