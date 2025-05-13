package com.summoner.lolhaeduo.client.service;

import com.summoner.lolhaeduo.client.dto.*;
import com.summoner.lolhaeduo.client.riot.RiotClient;
import com.summoner.lolhaeduo.common.limiter.RateLimiterManager;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile("inmemory")
@Slf4j
@RequiredArgsConstructor
public class InMemoryRiotApiQueueService implements RiotApiQueueService {

    private final RiotClient riotClient;
    private final RateLimiterManager rateLimiterManager;

    private final BlockingQueue<RiotApiRequest<?>> requestQueue = new LinkedBlockingQueue<>();
    // 스케줄러는 토큰 소비와 작업 디스패치만 담당
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 실제 API 호출은 별도 스레드 풀에서 처리
    private final ExecutorService apiExecutor = Executors.newCachedThreadPool();

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        // 큐 처리 스케줄러 시작 (2000ms마다)
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 2000, TimeUnit.MILLISECONDS);
        log.info("메모리 기반 Riot API Queue Service 초기화 완료");
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        apiExecutor.shutdown();
        log.info("메모리 기반 Riot API Queue Service 종료");
    }


    @Override
    public <T> CompletableFuture<T> enqueueRequest(RiotApiRequest.RequestType requestType, Map<String, Object> parameters) {
        RiotApiRequest<Object> request = RiotApiRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .requestType(requestType)
                .parameters(parameters)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .nextAttemptTime(LocalDateTime.now())
                .resultFuture(new CompletableFuture<>())
                .build();

        requestQueue.add(request);
        log.info("요청이 큐에 추가됨: {}, 유형: {}", request.getRequestId(), requestType);

        return (CompletableFuture<T>) request.getResultFuture();
    }

    private void processQueue() {
        if (isProcessing.compareAndSet(false, true)) {
            try {
                log.debug("큐 처리 시작, 현재 큐 크기: {}", requestQueue.size());

                while (!requestQueue.isEmpty()) {
                    RiotApiRequest<?> request = requestQueue.peek();

                    // 아직 처리 시간이 되지 않은 요청은 건너뜀
                    if (request.getNextAttemptTime().isAfter(LocalDateTime.now())) {
                        log.debug("요청 {}는 아직 처리 시간이 되지 않았습니다. 다음 시도: {}",
                                request.getRequestId(), request.getNextAttemptTime());
                        break;
                    }

                    // 레이트 리미터에서 토큰 소비 시도
                    ConsumptionProbe probe = rateLimiterManager.probe();

                    if (probe.isConsumed()) {
                        // 레이트 리밋 허용, 큐에서 제거
                        requestQueue.poll();
                        // 별도 스레드 풀에 작업 제출
                        apiExecutor.submit(() -> executeRequest(request));
                    } else {
                        // 다음 시도 시간 계산
                        long waitTimeMillis = probe.getNanosToWaitForRefill() / 1_000_000;
                        request.setNextAttemptTime(LocalDateTime.now().plusNanos(probe.getNanosToWaitForRefill()));

                        log.debug("레이트 리밋 발생, 요청 {} - {}ms 후 재시도",
                                request.getRequestId(), waitTimeMillis);
                        break;  // 기다려야 하므로 루프 종료
                    }
                }
            } catch (Exception e) {
                log.error("API 요청 큐 처리 중 오류 발생", e);
            } finally {
                isProcessing.set(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void executeRequest(RiotApiRequest<T> request) {
        try {
            long startTime = System.currentTimeMillis();
            log.debug("요청 {} 실행 시작, 유형: {}", request.getRequestId(), request.getRequestType());

            T result = switch (request.getRequestType()) {
                case EXTRACT_PUUID -> (T) executePuuidRequest(request);
                case EXTRACT_SUMMONER_INFO -> (T) executeSummonerInfoRequest(request);
                case EXTRACT_LEAGUE_INFO -> (T) executeLeagueInfoRequest(request);
                case EXTRACT_MATCH_IDS -> (T) executeMatchIdsRequest(request);
                case GET_MATCH_DETAILS -> (T) executeMatchDetailsRequest(request);
                default -> {
                    log.error("지원하지 않는 요청 유형: {}", request.getRequestType());
                    throw new IllegalArgumentException("지원하지 않는 요청 타입: " + request.getRequestType());
                }
            };

            request.getResultFuture().complete(result);
            long duration = System.currentTimeMillis() - startTime;
            log.info("요청 {} 성공적으로 완료됨, 소요 시간: {}ms", request.getRequestId(), duration);

        } catch (Exception e) {
            // 재시도 또는 실패 처리
            if (request.getRetryCount() < 3) {  // 최대 3회 재시도
                request.setRetryCount(request.getRetryCount() + 1);
                // 지수 백오프 적용
                long backoffMillis = (long) Math.pow(2, request.getRetryCount()) * 1000;
                request.setNextAttemptTime(LocalDateTime.now().plus(backoffMillis, ChronoUnit.MILLIS));
                requestQueue.add(request);
                log.warn("요청 {} 실패, 재시도 #{} - {}ms 후",
                        request.getRequestId(), request.getRetryCount(), backoffMillis);
            } else {
                log.error("요청 {} 최대 재시도 횟수 초과 실패", request.getRequestId(), e);
                request.getResultFuture().completeExceptionally(e);
            }
        }
    }


    // 각 요청 유형별 실행 메서드
    private PuuidResponse executePuuidRequest(RiotApiRequest request) {
        String summonerName = (String) request.getParameters().get("summonerName");
        String tagLine = (String) request.getParameters().get("tagLine");
        AccountRegion region = (AccountRegion) request.getParameters().get("region");

        log.debug("PUUID 요청 실행: summonerName={}, tagLine={}, region={}",
                summonerName, tagLine, region);

        return riotClient.extractPuuid(summonerName, tagLine, region);
    }

    private SummonerResponse executeSummonerInfoRequest(RiotApiRequest request) {
        String puuid = (String) request.getParameters().get("puuid");
        AccountServer server = (AccountServer) request.getParameters().get("server");

        log.debug("소환사 정보 요청 실행: puuid={}, server={}", puuid, server);

        return riotClient.extractSummonerInfo(puuid, server);
    }
    private List<LeagueEntryResponse> executeLeagueInfoRequest(RiotApiRequest request) {
        String summonerId = (String) request.getParameters().get("summonerId");
        AccountServer server = (AccountServer) request.getParameters().get("server");

        log.debug("리그 정보 요청 실행: summonerId={}, server={}", summonerId, server);

        return riotClient.extractLeagueInfo(summonerId, server);
    }

    @SuppressWarnings("unchecked")
    private List<String> executeMatchIdsRequest(RiotApiRequest request) {
        Long startTime = (Long) request.getParameters().get("startTime");
        Long endTime = (Long) request.getParameters().get("endTime");
        Integer queue = (Integer) request.getParameters().get("queue");
        String type = (String) request.getParameters().get("type");
        Integer start = (Integer) request.getParameters().get("start");
        Integer count = (Integer) request.getParameters().get("count");
        AccountRegion region = (AccountRegion) request.getParameters().get("region");
        String puuid = (String) request.getParameters().get("puuid");

        log.debug("매치 ID 요청 실행: puuid={}, region={}, count={}", puuid, region, count);
        return riotClient.extractMatchIds(startTime, endTime, queue, type, start, count, region, puuid);
    }

    private FormattedMatchResponse executeMatchDetailsRequest(RiotApiRequest request) {
        String matchId = (String) request.getParameters().get("matchId");
        String puuid = (String) request.getParameters().get("puuid");
        AccountRegion region = (AccountRegion) request.getParameters().get("region");

        log.debug("매치 상세 정보 요청 실행: matchId={}, puuid={}, region={}",
                matchId, puuid, region);
        return riotClient.getMatchDetails(matchId, puuid, region);
    }


}
