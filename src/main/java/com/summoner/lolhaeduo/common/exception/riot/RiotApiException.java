package com.summoner.lolhaeduo.common.exception.riot;

/**
 * Riot API 호출 관련 예외의 기본 클래스 입니다.
 */

public class RiotApiException extends RuntimeException {

    public RiotApiException(String message) {
        super(message);
    }

    public RiotApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RiotApiException(Throwable cause) {
        super(cause);
    }
}
