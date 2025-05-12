package com.summoner.lolhaeduo.common.exception.riot;

import jakarta.validation.constraints.NotNull;

/**
 * 소환사를 찾을 수 없을 때 발생하는 예외입니다.
 */

public class RiotApiSummonerNotFoundException extends RiotApiException {
    public RiotApiSummonerNotFoundException(String message, @NotNull String summonerName, @NotNull String tagLine) {
        super(message);
    }

    public RiotApiSummonerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RiotApiSummonerNotFoundException(Throwable cause) {
        super(cause);
    }
}
