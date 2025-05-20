package com.summoner.lolhaeduo.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Data
@NoArgsConstructor // Jackson이 객체를 역직렬화 할때 기본생성자 필요
@AllArgsConstructor
@Builder
public class RiotApiRequest<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String requestId;
    private RequestType requestType;
    private Map<String, Object> parameters;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime nextAttemptTime;
    private transient CompletableFuture<T> resultFuture;

    public enum RequestType {
        EXTRACT_PUUID,
        EXTRACT_SUMMONER_INFO,
        EXTRACT_LEAGUE_INFO,
        EXTRACT_MATCH_IDS,
        GET_MATCH_DETAILS
    }
}