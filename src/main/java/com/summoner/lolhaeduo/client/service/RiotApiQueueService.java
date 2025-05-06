package com.summoner.lolhaeduo.client.service;

import com.summoner.lolhaeduo.client.dto.RiotApiRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RiotApiQueueService {
    /**
     * API 요청을 큐에 추가합니다.
     * @param requestType 요청 유형
     * @param parameters 요청 파라미터
     * @return 요청 결과를 담고 있는 CompletableFuture
     */

    <T> CompletableFuture<T> enqueueRequest(RiotApiRequest.RequestType requestType, Map<String, Object> parameters);
}
