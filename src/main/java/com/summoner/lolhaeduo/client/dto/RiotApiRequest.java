package com.summoner.lolhaeduo.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Data
@Builder
public class RiotApiRequest<T> {
    private String requestId;
    private RequestType requestType;
    private Map<String, Object> parameters;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime nextAttemptTime;
    private CompletableFuture<T> resultFuture;

    public enum RequestType {
        EXTRACT_PUUID,
        EXTRACT_SUMMONER_INFO,
        EXTRACT_LEAGUE_INFO,
        EXTRACT_MATCH_IDS,
        GET_MATCH_DETAILS
    }
}