package com.summoner.lolhaeduo.common.exception.riot;

/**
 * Riot API 호출 처리 중 스레드가 중단되었을 때 발생하는 예외입니다.
 */
public class RiotApiInterruptedException extends RiotApiException {

    public RiotApiInterruptedException(String message) {
        super(message);
    }

    public RiotApiInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RiotApiInterruptedException(Throwable cause) {
        super(cause);
    }
}
