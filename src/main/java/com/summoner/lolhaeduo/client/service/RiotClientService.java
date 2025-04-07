package com.summoner.lolhaeduo.client.service;

import com.summoner.lolhaeduo.client.dto.*;
import com.summoner.lolhaeduo.client.entity.Favorite;
import com.summoner.lolhaeduo.client.repository.FavoriteRepository;
import com.summoner.lolhaeduo.client.repository.VersionRepository;
import com.summoner.lolhaeduo.client.riot.RiotClient;
import com.summoner.lolhaeduo.common.limiter.RateLimiterManager;
import com.summoner.lolhaeduo.common.util.TimeUtil;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountRequest;
import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.entity.RiotAccountInfo;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import com.summoner.lolhaeduo.common.exception.RateLimitExceededException;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public static final int RECENT_QUICK_MATCH_COUNT = 20;
    private static final int PERIOD_OF_RECENT_MATCH = 30;
    private static final int MAX_METHOD_CALL = 100;

    public RiotAccountInfo createRiotAccountInfo(LinkAccountRequest request) {

        ConsumptionProbe puuidProbe = rateLimiterManager.probe();
        if (log.isDebugEnabled()) {
            long waitMillis = puuidProbe.getNanosToWaitForRefill() / 1_000_000;
            String limitSource = waitMillis > 2000 ? "2분 제한 예상" : "1초 제한 예상";
            log.debug("[RateLimit] createRiotAccountInfo - 남은 토큰: {}, 대기 시간: {}ms, 원인: {}",
                    puuidProbe.getRemainingTokens(),
                    waitMillis, limitSource);
        }
        if (!puuidProbe.isConsumed()) {
            throw new RateLimitExceededException("Rate limit 초과 발생 ");
        }
        PuuidResponse puuidResponse = riotClient.extractPuuid(
                request.getSummonerName(),
                request.getTagLine(),
                request.getServer().getRegion()
        );

        if (!rateLimiterManager.tryConsume()) {
            throw new RateLimitExceededException("Rate limit 초과 발생 ");
        }
        SummonerResponse summonerResponse = riotClient.extractSummonerInfo(
                puuidResponse.getPuuid(),
                request.getServer()
        );

        return RiotAccountInfo.fromRiotApi(
                puuidResponse.getPuuid(),
                summonerResponse.getAccountId(),
                summonerResponse.getId()
        );
    }

    public String updateProfileIconUrl(Account account) {
        if (!rateLimiterManager.tryConsume()) {
            throw new RateLimitExceededException("Rate limit 초과 발생 ");
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
            throw new RateLimitExceededException("Rate limit 초과 발생 ");
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
            throw new RateLimitExceededException("Rate limit 초과 발생");
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
            throw new RateLimitExceededException("Rate limit 초과 발생 ");
        }
        long startTime = timeUtil.convertToEpochSeconds(lastUpdatedAt);
        return riotClient.extractMatchIds(
                startTime, null,
                queueType.getQueueId(),
                null, 0, 100, region, puuid
        );
    }

    @Transactional
    public MatchStats getMatchStats(Long accountId, List<String> matchIds, QueueType queueType, String summonerName, String tagLine, AccountRegion region) {
        int totalGames = matchIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<FormattedMatchResponse>> futures = new ArrayList<>();

        for (String matchId : matchIds) {
            Callable<FormattedMatchResponse> task = () -> fetchMatchData(matchId, summonerName, tagLine, region);
            futures.add(executorService.submit(task));
        }

        MatchStatAccumulator accumulator = collectMatchStats(futures);

        executorService.shutdown();

        double winRate = 0;
        if (queueType == QUICK) {
            winRate = accumulator.getWinRate(totalGames);
        }
        double averageKill = accumulator.getAverageKills(totalGames);
        double averageDeath = accumulator.getAverageDeaths(totalGames);
        double averageAssist = accumulator.getAverageAssists(totalGames);

        for (Map.Entry<String, Integer> entry : accumulator.getChampCount().entrySet()) {
            String championName = entry.getKey();
            int playCount = entry.getValue();
            int championWinCount = accumulator.getWinCountMap().getOrDefault(championName, 0);

            Favorite existingFavorite = favoriteRepository.findByAccountIdAndQueueTypeAndChampionName(accountId, queueType, championName);

            if (existingFavorite != null) {
                existingFavorite.update(playCount, championWinCount);
            } else {
                Favorite newFavorite = new Favorite(accountId, queueType, championName, playCount, championWinCount);
                favoriteRepository.save(newFavorite);
            }
        }

        return new MatchStats(
                accumulator.getWinCount(), totalGames - accumulator.getWinCount(), totalGames, queueType,
                winRate, averageKill, averageDeath, averageAssist
        );
    }

    private MatchStatAccumulator collectMatchStats(List<Future<FormattedMatchResponse>> futures) {
        int totalKills = 0, totalDeaths = 0, totalAssists = 0, winCount = 0;
        Map<String, Integer> champCount = new HashMap<>();
        Map<String, Integer> winCountMap = new HashMap<>();

        for (Future<FormattedMatchResponse> future : futures) {
            try {
                FormattedMatchResponse result = future.get();
                if (result != null) {
                    champCount.put(result.getChampionName(), champCount.getOrDefault(result.getChampionName(), 0) + 1);
                    totalKills += result.getKills();
                    totalDeaths += result.getDeaths();
                    totalAssists += result.getAssists();

                    if (result.isWin()) {
                        winCount++;
                        winCountMap.put(result.getChampionName(), winCountMap.getOrDefault(result.getChampionName(), 0) + 1);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("current error: {}", e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return new MatchStatAccumulator(totalKills, totalDeaths, totalAssists, winCount, champCount, winCountMap);
    }

    private FormattedMatchResponse fetchMatchData(String matchId, String summonerName, String tagLine, AccountRegion region) {
        ConsumptionProbe matchDataProbe = rateLimiterManager.probe();
        if (log.isDebugEnabled()) {
            long waitMillis = matchDataProbe.getNanosToWaitForRefill() / 1_000_000;
            String limitSource = waitMillis > 2000 ? "2분 제한 예상" : "1초 제한 예상";
            log.debug("[RateLimit] fetchMatchData - matchId: {}, 남은 토큰: {}, 대기 시간: {}ms, 원인: {}",
                    matchId,
                    matchDataProbe.getRemainingTokens(),
                    waitMillis, limitSource);
        }
        if (!matchDataProbe.isConsumed()) {
            throw new RateLimitExceededException("Rate limit 초과 발생 ");
        }
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();

        log.info("Thread ID: {}, Name: {} is processing matchId: {}", threadId, threadName, matchId);

        FormattedMatchResponse matchResponse = riotClient.getMatchDetails(matchId, summonerName, tagLine, region);

        long duration = System.currentTimeMillis() - startTime;
        log.info("matchId: {} 처리 완료 ({} ms)", matchId, duration);

        if (matchResponse != null) {
            return new FormattedMatchResponse(
                    matchResponse.getChampionName(),
                    matchResponse.getKills(),
                    matchResponse.getDeaths(),
                    matchResponse.getAssists(),
                    matchResponse.isWin()
            );
        }
        log.warn("matchId: {} 처리 실패 - Riot API 재시도 후에도 실패했거나 유효하지 않은 매치입니다", matchId);
        return null;
    }

    private static class MatchStatAccumulator {
        private final int totalKills;
        private final int totalDeaths;
        private final int totalAssists;
        private final int winCount;
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

        public int getWinCount() {
            return winCount;
        }

        public Map<String, Integer> getChampCount() {
            return champCount;
        }

        public Map<String, Integer> getWinCountMap() {
            return winCountMap;
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
    }
}
