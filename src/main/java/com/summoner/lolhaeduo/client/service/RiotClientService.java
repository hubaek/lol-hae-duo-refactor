package com.summoner.lolhaeduo.client.service;

import com.summoner.lolhaeduo.client.dto.*;
import com.summoner.lolhaeduo.client.entity.Favorite;
import com.summoner.lolhaeduo.client.repository.FavoriteRepository;
import com.summoner.lolhaeduo.client.repository.VersionRepository;
import com.summoner.lolhaeduo.client.riot.RiotClient;
import com.summoner.lolhaeduo.common.exception.riot.*;
import com.summoner.lolhaeduo.common.limiter.RateLimiterManager;
import com.summoner.lolhaeduo.common.util.TimeUtil;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountRequest;
import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.entity.RiotAccountInfo;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import io.github.bucket4j.ConsumptionProbe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.summoner.lolhaeduo.domain.duo.enums.QueueType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiotClientService {

    private final RiotClient riotClient;
    private final TimeUtil timeUtil;
    private final VersionRepository versionRepository;
    private final FavoriteRepository favoriteRepository;
    private final RateLimiterManager rateLimiterManager;
    private final RiotApiQueueService apiQueueService;

    public static final int RECENT_QUICK_MATCH_COUNT = 20;
    private static final int PERIOD_OF_RECENT_MATCH = 30;
    private static final int MAX_METHOD_CALL = 100;

    public RiotAccountInfo createRiotAccountInfo(LinkAccountRequest request) {

        try {
            // 1. PUUID 요청을 큐에 등록
            Map<String, Object> puuidParams = new HashMap<>();
            puuidParams.put("summonerName", request.getSummonerName());
            puuidParams.put("tagLine", request.getTagLine());
            puuidParams.put("region", request.getServer().getRegion());

            PuuidResponse puuidResponse = (PuuidResponse) apiQueueService.enqueueRequest(
                    RiotApiRequest.RequestType.EXTRACT_PUUID, puuidParams).get();  // 블로킹 호출

            if (puuidResponse == null || puuidResponse.getPuuid() == null) {
                throw new RiotApiSummonerNotFoundException("소환사 정보를 찾을 수 없습니다: %s$%s",
                        request.getSummonerName(), request.getTagLine());
            }

            // 2. 소환사 정보 요청을 큐에 등록
            Map<String, Object> summonerParams = new HashMap<>();
            summonerParams.put("puuid", puuidResponse.getPuuid());
            summonerParams.put("server", request.getServer());

            SummonerResponse summonerResponse = (SummonerResponse) apiQueueService.enqueueRequest(
                    RiotApiRequest.RequestType.EXTRACT_SUMMONER_INFO, summonerParams).get();  // 블로킹 호출

            if (summonerResponse == null || summonerResponse.getId() == null) {
                throw new RiotApiSummonerNotFoundException("PUUID에 해당하는 소환사 정보를 찾을 수 없습니다." + puuidResponse.getPuuid(), request.getSummonerName(), request.getTagLine());
            }

            return RiotAccountInfo.fromRiotApi(
                    puuidResponse.getPuuid(),
                    summonerResponse.getAccountId(),
                    summonerResponse.getId()
            );
        } catch (InterruptedException e) {
            log.error("Riot 계정 정보 생성 중 스레드 중단", e);
            Thread.currentThread().interrupt();
            throw new RiotApiInterruptedException("소환사 정보 조회 중 작업이 중단되었습니다.", e);
        } catch (ExecutionException e) {
            log.error("Riot 계정 정보 생성 중 오류 발생", e);
            Throwable cause = e.getCause();
            throw new RiotApiException("소환사 정보 조회 중 오류 발생", cause);
        }
    }

    public String updateProfileIconUrl(Account account) {
        if (!rateLimiterManager.tryConsume()) {
            throw new InternalRateLimitException("Rate limit 초과 발생 ");
        }
        SummonerResponse response = riotClient.extractSummonerInfo(account.getRiotAccountInfo().getPuuid(), account.getServer());
        int accountProfileIconId = response.getProfileIconId();

        String latestVersion = versionRepository.findLatestVersion().getVersionNumber();

        return String.format(
                "https://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png",
                latestVersion, accountProfileIconId
        );
    }

    public RankStats getRankGameStats(String summonerId, AccountServer server) {
        if (!rateLimiterManager.tryConsume()) {
            throw new InternalRateLimitException("Rate limit 초과 발생 ");
        }
        List<LeagueEntryResponse> leagueInfoList = riotClient.extractLeagueInfo(summonerId, server);

        int soloTotalGames = 0, flexTotalGames = 0;
        int soloWins = 0, soloLosses = 0;
        int flexWins = 0, flexLosses = 0;
        String soloTier = "", soloRank = "";
        String flexTier = "", flexRank = "";

        for (LeagueEntryResponse leagueInfo : leagueInfoList) {
            int wins = leagueInfo.getWins();
            int losses = leagueInfo.getLosses();

            if (leagueInfo.getQueueType().equals(SOLO.getQueueType())) {
                soloWins = wins;
                soloLosses = losses;
                soloTotalGames = wins + losses;
                soloTier = leagueInfo.getTier();
                soloRank = leagueInfo.getRank();
            } else if (leagueInfo.getQueueType().equals(FLEX.getQueueType())) {
                flexWins = wins;
                flexLosses = losses;
                flexTotalGames = wins + losses;
                flexTier = leagueInfo.getTier();
                flexRank = leagueInfo.getRank();
            }
        }

        double soloWinRate = 0;
        double flexWinRate = 0;
        if ((soloWins + soloLosses) > 0) {
            soloWinRate = (double) soloWins / (soloWins + soloLosses) * 100;
        }
        if ((flexWins + flexLosses) > 0) {
            flexWinRate = (double) flexWins / (flexWins + flexLosses) * 100;
        }

        return new RankStats(soloTotalGames, flexTotalGames, soloWins, soloLosses, soloWinRate, soloTier, soloRank, flexWins, flexLosses, flexWinRate, flexTier, flexRank);
    }

    public List<String> getMatchIds(QueueType queueType, AccountRegion region, String puuid, int matchCount) {
        ConsumptionProbe matchIdsProbe = rateLimiterManager.probe();
        if (log.isDebugEnabled()) {
            long waitMillis = matchIdsProbe.getNanosToWaitForRefill() / 1_000_000;
            String limitSource = waitMillis > 2000 ? "2분 제한 예상" : "1초 제한 예상";
            log.debug("[RateLimit] getMatchIds - 남은 토큰: {}, 대기 시간: {}ms, 원인: {}",
                    matchIdsProbe.getRemainingTokens(), waitMillis, limitSource);
        }
        if (!matchIdsProbe.isConsumed()) {
            throw new InternalRateLimitException("Rate limit 초과 발생");
        }

        if (queueType == QUICK) {
            return riotClient.extractMatchIds(
                    null, null,
                    queueType.getQueueId(),
                    null, 0, RECENT_QUICK_MATCH_COUNT, region, puuid
            );
        }

        List<String> allMatchIds = new ArrayList<>();
        int start = 0;
        while (start < matchCount) {
            int count = Math.min(MAX_METHOD_CALL, matchCount - start);
            List<String> partialMatchIds
                    = riotClient.extractMatchIds(
                    null, null,
                    queueType.getQueueId(),
                    null, start, count, region, puuid
            );
            if (partialMatchIds.isEmpty()) {
                break;
            }
            allMatchIds.addAll(partialMatchIds);
            start += count;
        }
        return allMatchIds;
    }

    public List<String> updateMatchIds(QueueType queueType, LocalDateTime lastUpdatedAt, AccountRegion region, String puuid) {
        if (!rateLimiterManager.tryConsume()) {
            throw new InternalRateLimitException("Rate limit 초과 발생 ");
        }
        long startTime = timeUtil.convertToEpochSeconds(lastUpdatedAt);
        return riotClient.extractMatchIds(
                startTime, null,
                queueType.getQueueId(),
                null, 0, 100, region, puuid
        );
    }

    @Transactional
    public MatchStats getMatchStats(Long accountId, List<String> matchIds, QueueType queueType, String puuid, AccountRegion region) {
        int totalGames = matchIds.size();
        // 1. 요청 등록
        List<CompletableFuture<FormattedMatchResponse>> futures = enqueueMatchRequests(matchIds, puuid, region);

        try {
            // 2. 모든 비동기 요청이 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            // 3. 결과 처리
            MatchResultProcessingResult result = processMatchResults(futures);

            if (!result.getFailedMatchIds().isEmpty()) {
                log.warn("일부 매치 데이터를 가져오지 못했습니다. 실패한 매치 ID: {}", result.getFailedMatchIds());
            }

            // 4. Favorite 엔티티 업데이트
            updateFavoriteEntities(accountId, queueType, result.getAccumulator());

            // 5. 통계 계산 및 반환
            return calculateMatchStats(queueType, totalGames, result.getAccumulator());
        } catch (InterruptedException e) {
            log.error("매치 통계 처리 중 스레드 중단", e);
            Thread.currentThread().interrupt();
            throw new RiotApiInterruptedException("매치 데이터 처리 중 작업이 중단되었습니다", e);
        } catch (ExecutionException e) {
            log.error("매치 통계 처리 중 오류 발생", e);
            Throwable cause = e.getCause();
            if (cause instanceof InternalRateLimitException) {
                throw new RiotApiRateLimitException("Riot API 레이트 리밋 초과", cause);
            }
            if (cause instanceof IOException) {
                throw new RiotApiException("Riot API 통신 오류", cause);
            }
            throw new RiotApiException("매치 데이터 처리 중 오류 발생", e);
        }
    }

    // 1. 요청 등록 메서드
    private List<CompletableFuture<FormattedMatchResponse>> enqueueMatchRequests(List<String> matchIds, String puuid, AccountRegion region) {
        List<CompletableFuture<FormattedMatchResponse>> futures = new ArrayList<>();
        for (String matchId : matchIds) {
            Map<String, Object> params = new HashMap<>();
            params.put("matchId", matchId);
            params.put("puuid", puuid);
            params.put("region", region);

            futures.add(apiQueueService.enqueueRequest(
                    RiotApiRequest.RequestType.GET_MATCH_DETAILS, params));
        }
        return futures;
    }

    // 3. 결과 처리 메서드
    private static MatchResultProcessingResult processMatchResults(List<CompletableFuture<FormattedMatchResponse>> futures) {
        MatchStatAccumulator accumulator = new MatchStatAccumulator(0, 0, 0, 0, new HashMap<>(), new HashMap<>());
        List<String> failedMatchIds = new ArrayList<>();

        for (CompletableFuture<FormattedMatchResponse> future : futures) {
            try {
                FormattedMatchResponse result = future.get();
                if (result != null) {
                    // 챔피언 카운트 업데이트
                    accumulator.getChampCount().put(
                            result.getChampionName(),
                            accumulator.getChampCount().getOrDefault(result.getChampionName(), 0) + 1);

                    // KDA 업데이트
                    accumulator.addTotalKills(result.getKills());
                    accumulator.addTotalDeaths(result.getDeaths());
                    accumulator.addTotalAssists(result.getAssists());

                    // 승리 카운트 및 챔피언별 승리 카운트 업데이트
                    if (result.isWin()) {
                        accumulator.incrementWinCount();
                        accumulator.getWinCountMap().put(
                                result.getChampionName(),
                                accumulator.getWinCountMap().getOrDefault(result.getChampionName(), 0) + 1);
                    }
                } else {
                    // null 결과는 실패로 간주
                    failedMatchIds.add(future.toString());
                    log.warn("매치 결과가 null입니다. 매치 ID: {}", future.toString());
                }
            } catch (InterruptedException e) {
                failedMatchIds.add("InterruptedException: " + future.toString());
                log.error("매치 결과 처리 중 스레드 중단", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                failedMatchIds.add("Failed to get match result: " + future.toString());
                log.error("매치 결과 처리 중 오류", e);
            }
        }
        return new MatchResultProcessingResult(accumulator, failedMatchIds);
    }

    // 4. Favorite 엔티티 업데이트 메서드
    private void updateFavoriteEntities(Long accountId, QueueType queueType, MatchStatAccumulator accumulator) {
        for (Map.Entry<String, Integer> entry : accumulator.getChampCount().entrySet()) {
            String championName = entry.getKey();
            int playCount = entry.getValue();
            int championWinCount = accumulator.getWinCountMap().getOrDefault(championName, 0);

            Favorite existingFavorite = favoriteRepository.findByAccountIdAndQueueTypeAndChampionName(
                    accountId, queueType, championName);

            if (existingFavorite != null) {
                existingFavorite.update(playCount, championWinCount);
            } else {
                Favorite newFavorite = new Favorite(
                        accountId, queueType, championName, playCount, championWinCount);
                favoriteRepository.save(newFavorite);
            }
        }
    }

    // 5. 통계 계산 메서드
    private static MatchStats calculateMatchStats(QueueType queueType, int totalGames, MatchStatAccumulator accumulator) {
        double winRate = queueType == QUICK ? accumulator.getWinRate(totalGames) : 0;
        double averageKill = accumulator.getAverageKills(totalGames);
        double averageDeath = accumulator.getAverageDeaths(totalGames);
        double averageAssist = accumulator.getAverageAssists(totalGames);

        return new MatchStats(
                accumulator.getWinCount(),
                totalGames - accumulator.getWinCount(),
                totalGames,
                queueType,
                winRate,
                averageKill,
                averageDeath,
                averageAssist
        );
    }

    @Getter
    private static class MatchResultProcessingResult {

        private final MatchStatAccumulator accumulator;
        private final List<String> failedMatchIds;

        public MatchResultProcessingResult(MatchStatAccumulator accumulator, List<String> failedMatchIds) {
            this.accumulator = accumulator;
            this.failedMatchIds = failedMatchIds;
        }

    }

    @Getter
    private static class MatchStatAccumulator {

        private int totalKills;
        private int totalDeaths;
        private int totalAssists;
        private int winCount;
        private final Map<String, Integer> champCount;
        private final Map<String, Integer> winCountMap;

        MatchStatAccumulator(int totalKills, int totalDeaths, int totalAssists, int winCount,
                             Map<String, Integer> champCount, Map<String, Integer> winCountMap) {
            this.totalKills = totalKills;
            this.totalDeaths = totalDeaths;
            this.totalAssists = totalAssists;
            this.winCount = winCount;
            this.champCount = champCount;
            this.winCountMap = winCountMap;
        }

        public double getAverageKills(int totalGames) {
            return totalGames == 0 ? 0 : (double) totalKills / totalGames;
        }

        public double getAverageDeaths(int totalGames) {
            return totalGames == 0 ? 0 : (double) totalDeaths / totalGames;
        }

        public double getAverageAssists(int totalGames) {
            return totalGames == 0 ? 0 : (double) totalAssists / totalGames;
        }

        public double getWinRate(int totalGames) {
            return totalGames == 0 ? 0 : (double) winCount / totalGames * 100;
        }

        public void addTotalKills(int kills) {
            this.totalKills = kills;
        }

        public void addTotalDeaths(int deaths) {
            this.totalDeaths = deaths;
        }

        public void addTotalAssists(int assists) {
            this.totalAssists = assists;
        }

        public void incrementWinCount() {
            this.winCount++;
        }

    }

}
