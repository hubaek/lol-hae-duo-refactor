package com.summoner.lolhaeduo.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summoner.lolhaeduo.client.dto.RiotApiRequest;
import com.summoner.lolhaeduo.client.riot.RiotClient;
import com.summoner.lolhaeduo.common.limiter.RateLimiterManager;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
@Slf4j
@RequiredArgsConstructor
public class RedisRiotApiQueueService implements RiotApiQueueService {

    private final RiotClient riotClient;
    private final RateLimiterManager rateLimiterManager;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    // 결과를 저장할 Future Map
    private final Map<String, CompletableFuture<?>> futureMap = new ConcurrentHashMap<>();

    // 큐 이름
    private static final String QUEUE_NAME = "riot_api_queue";
    private static final String DELAYED_QUEUE_NAME = "riot_api_delayed_queue";

    // 레이트 리밋 상태 추적
    private boolean isRateLimited = false;
    private long lastLogTime = 0;
    private static final long LOG_INTERVAL = 10_000; // 10초 마다 로그 출력

    @Override
    public <T> CompletableFuture<T> enqueueRequest(RiotApiRequest.RequestType requestType, Map<String, Object> parameters) {
        // Enum 타입을 문자열로 반환
        Map<String, Object> serializedParams = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (value instanceof Enum<?>) {
                serializedParams.put(key, ((Enum<?>) value).name());
            } else {
                serializedParams.put(key, value);
            }
        });

        String requestId = UUID.randomUUID().toString();
        CompletableFuture<T> future = new CompletableFuture<>();

        // Future Map에 저장
        futureMap.put(requestId, future);

        // 요청 생성
        RiotApiRequest<T> request = RiotApiRequest.<T>builder()
                .requestId(requestId)
                .requestType(requestType)
                .parameters(serializedParams)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .nextAttemptTime(LocalDateTime.now())
                .build();

        // 큐에 추가
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(QUEUE_NAME);
        try {
            String serializedRequest = objectMapper.writeValueAsString(request);
            queue.add(serializedRequest);
            log.info("요청이 Redis 큐에 추가: {}, 유형: {}", requestId, requestType);
        } catch (Exception e) {
            log.error("요청을 큐에 추가하는 중 오류 발생: {}", e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    // 주기적으로 큐에서 요청을 처리
    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(QUEUE_NAME);

        try {
            int queueSize = queue.size();
            if (queueSize == 0) {
                // 주기적으로만 로그 출력 (불필요한 로그 감소)
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLogTime > LOG_INTERVAL) {
                    log.debug("큐가 비어있음, 처리할 요청 없음");
                    lastLogTime = currentTime;
                }
                return;
            }
            log.debug("큐에서 요청 처리 시작, 현재 큐 크기: {}", queueSize);

            // 레이트 리미터 확인
            ConsumptionProbe probe = rateLimiterManager.probe();

            if (probe.isConsumed()) {
                // 토큰이 있으면 요청 처리
                isRateLimited = false;
                String serializedRequest = queue.poll();
                if (serializedRequest != null) {
                    processRequest(serializedRequest);
                }
            } else {
                // 레이트 리밋에 걸렸을 때 잠시 대기
                long waitTimeMs = probe.getNanosToWaitForRefill() / 1_000_000;

                // 상태 변경 시나 주기적으로만 로그 출력
                if (!isRateLimited || (System.currentTimeMillis() - lastLogTime > LOG_INTERVAL)) {
                    log.debug("레이트 리밋 발생, {}ms 후 재시도", waitTimeMs);
                    lastLogTime = System.currentTimeMillis();
                }
                isRateLimited = true;
            }
        } catch (Exception e) {
            log.error("큐 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void processRequest(String serializedRequest) {
        try {
            RiotApiRequest<?> request = objectMapper.readValue(serializedRequest, RiotApiRequest.class);
            executeRequest(request);

        } catch (Exception e) {
            log.error("요청 처리 중 오류 발생", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void executeRequest(RiotApiRequest<T> request) {
        try {
            log.debug("요청 {} 실행 시작, 유형: {}", request.getRequestId(), request.getRequestType());

            // Future 맵에서 해당 요청의 Future 가져오기
            CompletableFuture<T> future = (CompletableFuture<T>) futureMap.get(request.getRequestId());
            if (future == null) {
                log.warn("요청 {}에 대한 Future를 찾을 수 없습니다.", request.getRequestId());
                return;
            }

            // 요청 유형에 따라 처리
            T result = switch (request.getRequestType()) {
                case EXTRACT_PUUID -> executePuuidRequest(request);
                case EXTRACT_SUMMONER_INFO -> executeSummonerInfoRequest(request);
                case EXTRACT_LEAGUE_INFO -> executeLeagueInfoRequest(request);
                case EXTRACT_MATCH_IDS -> executeMatchIdsRequest(request);
                case GET_MATCH_DETAILS -> executeMatchDetailsRequest(request);
                default -> {
                    log.error("지원하지 않는 요청 유형: {}", request.getRequestType());
                    throw new IllegalArgumentException("지원하지 않는 요청 타입: " + request.getRequestType());
                }
            };

            // 결과를 Future에 설정
            future.complete(result);
            futureMap.remove(request.getRequestId());

            log.info("요청 {} 성공적으로 완료됨", request.getRequestId());

        } catch (Exception e) {
            // 재시도 로직
            handleRequestFailure(request, e);
        }
    }

    private <T> void handleRequestFailure(RiotApiRequest<T> request, Exception e) {
        // 최대 3회까지 재시도
        if (request.getRetryCount() < 3) {
            request.setRetryCount(request.getRetryCount() + 1);
            // 지수 백오프 적용
            long backoffMillis = (long) Math.pow(2, request.getRetryCount()) * 1000;
            request.setNextAttemptTime(LocalDateTime.now().plus(backoffMillis, ChronoUnit.MILLIS));

            // 지연 큐에 추가
            try {
                RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(
                        redissonClient.getBlockingQueue(DELAYED_QUEUE_NAME));
                String serializedRequest = objectMapper.writeValueAsString(request);
                delayedQueue.offer(serializedRequest, backoffMillis, TimeUnit.MILLISECONDS);
                log.warn("요청 {} 실패, 재시도 #{} - {}ms 후", request.getRequestId(), request.getRetryCount(), backoffMillis);
            } catch (Exception serializeEx) {
                log.error("요청 재시도 큐 추가 중 오류", serializeEx);
                completeExceptionally(request, e);
            }
        } else {
            log.error("요청 {} 최대 재시도 횟수 초과 실패", request.getRequestId(), e);
            completeExceptionally(request, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void completeExceptionally(RiotApiRequest<T> request, Exception e) {
        CompletableFuture<T> future = (CompletableFuture<T>) futureMap.get(request.getRequestId());
        if (future != null) {
            future.completeExceptionally(e);
            futureMap.remove(request.getRequestId());
        }
    }

    // 지연 큐에서 메인 큐로 이동하는 스케줄러
    @Scheduled(fixedDelay = 5000)
    public void processDelayedQueue() {
        try {
            RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(
                    redissonClient.getBlockingQueue(DELAYED_QUEUE_NAME));
            RBlockingQueue<String> mainQueue = redissonClient.getBlockingQueue(QUEUE_NAME);

            // 큐 크기 확인
            int delayedQueueSize = delayedQueue.size();
            if (delayedQueueSize == 0) {
                return; // 지연 큐가 비어있으면 처리하지 않음
            }

            log.debug("지연 큐에서 요청 처리 시작, 크기: {}", delayedQueueSize);

            // 지연 큐에서 처리할 최대 항목 수 제한
            int processed = 0;
            int maxToProcess = Math.min(delayedQueueSize, 10); // 최대 10개 항목 처리

            String request = delayedQueue.poll();
            while (request != null && processed < maxToProcess) {
                mainQueue.add(request);
                processed++;
                request = delayedQueue.poll();
            }

            if (processed > 0) {
                log.info("지연 큐에서 메인 큐로 {} 항목 이동", processed);
            }

        } catch (Exception e) {
            log.error("지연 큐 처리 중 오류 발생", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T executePuuidRequest(RiotApiRequest<T> request) {
        String summonerName = (String) request.getParameters().get("summonerName");
        String tagLine = (String) request.getParameters().get("tagLine");

        // 문자열에서 Enum으로 복원
        String regionString = (String) request.getParameters().get("region");
        AccountRegion region = AccountRegion.valueOf(regionString);

        log.debug("PUUID 요청 실행: summonerName={}, tagLine={}, region={}", summonerName, tagLine, region);

        return (T) riotClient.extractPuuid(summonerName, tagLine, region);
    }

    @SuppressWarnings("unchecked")
    private <T> T executeSummonerInfoRequest(RiotApiRequest<T> request) {
        String puuid = (String) request.getParameters().get("puuid");

        // 문자열에서 Enum으로 복원
        String serverString = (String) request.getParameters().get("server");
        AccountServer server = AccountServer.valueOf(serverString);

        log.debug("소환사 정보 요청 실행: puuid={}, server={}", puuid, server);

        return (T) riotClient.extractSummonerInfo(puuid, server);
    }

    @SuppressWarnings("unchecked")
    private <T> T executeLeagueInfoRequest(RiotApiRequest<T> request) {
        String summonerId = (String) request.getParameters().get("summonerId");

        // 문자열에서 Enum으로 복원
        String serverString = (String) request.getParameters().get("server");
        AccountServer server = AccountServer.valueOf(serverString);

        log.debug("리그 정보 요청 실행: summonerId={}, server={}", summonerId, server);

        return (T) riotClient.extractLeagueInfo(summonerId, server);
    }

    @SuppressWarnings("unchecked")
    private <T> T executeMatchIdsRequest(RiotApiRequest<T> request) {
        Long startTime = (Long) request.getParameters().get("startTime");
        Long endTime = (Long) request.getParameters().get("endTime");
        Integer queue = (Integer) request.getParameters().get("queue");
        String type = (String) request.getParameters().get("type");
        Integer start = (Integer) request.getParameters().get("start");
        Integer count = (Integer) request.getParameters().get("count");

        // 문자열에서 Enum으로 복원
        String regionString = (String) request.getParameters().get("region");
        AccountRegion region = AccountRegion.valueOf(regionString);
        String puuid = (String) request.getParameters().get("puuid");

        log.debug("매치 ID 요청 실행: puuid={}, region={}, count={}", puuid, region, count);
        return (T) riotClient.extractMatchIds(startTime, endTime, queue, type, start, count, region, puuid);
    }

    @SuppressWarnings("unchecked")
    private <T> T executeMatchDetailsRequest(RiotApiRequest<T> request) {
        String matchId = (String) request.getParameters().get("matchId");
        String puuid = (String) request.getParameters().get("puuid");

        // 문자열에서 Enum으로 복원
        String regionString = (String) request.getParameters().get("region");
        AccountRegion region = AccountRegion.valueOf(regionString);

        log.debug("매치 상세 정보 요청 실행: matchId={}, puuid={}, region={}", matchId, puuid, region);
        return (T) riotClient.getMatchDetails(matchId, puuid, region);
    }

}
