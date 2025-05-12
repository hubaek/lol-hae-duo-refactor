package com.summoner.lolhaeduo.common.exception.riot;

/**
 * 어플리케이션 내부에서 발생한 Rate Limit 예외입니다.
 */

public class InternalRateLimitException extends RuntimeException {
    public InternalRateLimitException(String message) {
        super(message);
    }
}