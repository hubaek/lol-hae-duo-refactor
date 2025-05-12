package com.summoner.lolhaeduo.common.exception.riot;

/**
 * Riot API 호출 시 레이트 리밋을 초과했을 때 발생하는 예외입니다.
 */

public class RiotApiRateLimitException extends RiotApiException {

    public RiotApiRateLimitException(String message) {
        super(message);
    }

    public RiotApiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RiotApiRateLimitException(Throwable cause) {
        super(cause);
    }
}
