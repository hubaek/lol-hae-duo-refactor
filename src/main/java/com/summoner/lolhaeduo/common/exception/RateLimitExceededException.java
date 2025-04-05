package com.summoner.lolhaeduo.common.exception;


public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}