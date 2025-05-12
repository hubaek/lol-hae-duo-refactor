package com.summoner.lolhaeduo.common.exception.riot;


public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}