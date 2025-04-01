package com.summoner.lolhaeduo.client.service;

import com.summoner.lolhaeduo.client.dto.*;
import com.summoner.lolhaeduo.client.entity.Favorite;
import com.summoner.lolhaeduo.client.repository.FavoriteRepository;
import com.summoner.lolhaeduo.client.repository.VersionRepository;
import com.summoner.lolhaeduo.client.riot.RiotClient;
import com.summoner.lolhaeduo.common.util.TimeUtil;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountRequest;
import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.entity.RiotAccountInfo;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
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

    private static final int NUMBER_OF_RECENT_MATCH = 20;
    private static final int PERIOD_OF_RECENT_MATCH = 30;
    private static final int MAX_METHOD_CALL = 100;

    public RiotAccountInfo createRiotAccountInfo(LinkAccountRequest request) {
        PuuidResponse puuidResponse = riotClient.extractPuuid(
                request.getSummonerName(),
                request.getTagLine(),
                request.getServer().getRegion()
        );

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

    // todo
    //  처음 account 생성할 때 profileIconUrl을 가져오는 로직을 추가해야함?
    //  업데이트에 SummonerResponse를 사용하고 있는데, 이걸로 가져오는게 맞는지 확인 필요
    //

    public String updateProfileIconUrl(Account account) {
        SummonerResponse response = riotClient.extractSummonerInfo(account.getRiotAccountInfo().getPuuid(), account.getServer());
        int accountProfileIconId = response.getProfileIconId();

        String latestVersion = versionRepository.findLatestVersion().getVersionNumber();

        return String.format(
                "https://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png",
                latestVersion, accountProfileIconId
        );
    }

    public RankStats getRankGameStats(String summonerId, AccountServer server) {
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
            }
            else if (leagueInfo.getQueueType().equals(FLEX.getQueueType())) {
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

    public List<String> getMatchIds(QueueType queueType, int playCount, AccountRegion region, String puuid) {
        List<String> matchIds = new ArrayList<>();
        int totalRetrieved = 0;

        // todo
        //  1. playCount가 0인 경우는 어떻게 처리할지 고민해보기
        //  1-1. playCount가 0인 경우는 아예 API를 호출하지 않음
        //  2. count = 5로 고정해서 최근 5판만 가져오도록 수정
        //  3. if (queueType == QUICK) else구문을 없애고, queueType에 따라 다르게 처리하도록 수정
        //  3-1. queueType이 QUICK인 경우는 quickGameData를 가져오도록 수정
        //  3-2. queueType이 SOLO인 경우는 soloRankData를 가져오고, FLEX인 경우는 flexRankData를 가져오도록 수정
        //  4. if문에 totalRetrieved는 추후에 추가할 수 있도록 로직 수정 (현재는 최근 5판만)

        if (queueType == QUICK) {
            int start = 0;
            while (true) {
                List<String> partialMatchIds = riotClient.extractMatchIds(null, null, QUICK.getQueueId(), null, start, 100, region, puuid);
                if (partialMatchIds == null || partialMatchIds.isEmpty()) {
                    break;
                }

                matchIds.addAll(partialMatchIds);
                totalRetrieved += partialMatchIds.size();
                start += totalRetrieved;
            }
        } else {
            // 랭크 게임의 경우 playCount에 따라 100판 단위로 API 호출
            // todo : count = 5로 고정해서 최근 5판만 가져오도록 수정
            //  -> 추후 Production Key 받으면 전체 판 수 가져오기

            while (totalRetrieved < playCount) {
                int count = Math.min(MAX_METHOD_CALL, playCount - totalRetrieved);
                List<String> partialMatchIds = riotClient.extractMatchIds(
                        null, null,
                        queueType == SOLO ? SOLO.getQueueId() : FLEX.getQueueId(),

                        null, totalRetrieved,
                        count, region, puuid
                );

                if (partialMatchIds == null || partialMatchIds.isEmpty()) {
                    break;
                }

                matchIds.addAll(partialMatchIds);
                totalRetrieved += partialMatchIds.size();

                // 만약 가져온 판 수가 예상보다 적다면 더 이상 호출할 필요가 없음
                if (partialMatchIds.size() < count) {
                    break;
                }
            }
        }

        return matchIds;
    }

    // 수동으로 전적 정보 갱신 시 하루 안에 100번 게임을 할 가능성이 없다고 판단했습니다.
    // 따라서 RiotClient의 getmatchIds를 한번만 호출해도 필요한 모든 정보를 다 조회할 수 있다고 생각해서, 1번만 호출하게 되었습니다.
    public List<String> updateMatchIds(QueueType queueType, LocalDateTime lastUpdatedAt, AccountRegion region, String puuid) {
        long startTime = timeUtil.convertToEpochSeconds(lastUpdatedAt);
        return riotClient.extractMatchIds(
                startTime, null,
                queueType.getQueueId(),
                null, 0, 100, region, puuid
        );
    }

    @Transactional
    public MatchStats getMatchStats(Long accountId, List<String> matchIds, QueueType queueType, String summonerName, String tagLine, AccountRegion region) {
        // 초기화
        int totalKills = 0, totalDeath = 0, totalAssists = 0, winCount = 0;
        int totalGames = matchIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<FormattedMatchResponse>> futures = new ArrayList<>();

        for (String matchId : matchIds) {
            Callable<FormattedMatchResponse> task = () -> {
                long threadId = Thread.currentThread().getId();
                String threadName = Thread.currentThread().getName();
                log.info("Thread ID: {}, Name: {} is processing matchId: {}", threadId, threadName, matchId);

                FormattedMatchResponse matchResponse = riotClient.getMatchDetails(matchId, summonerName, tagLine, region);
                if (matchResponse != null) {
                    return new FormattedMatchResponse(
                            matchResponse.getChampionName(),
                            matchResponse.getKills(),
                            matchResponse.getDeaths(),
                            matchResponse.getAssists(),
                            matchResponse.isWin()
                    );
                }
                return null;
            };
            futures.add(executorService.submit(task));
        }

        Map<String, Integer> champCount = new HashMap<>();
        Map<String, Integer> winCountMap = new HashMap<>();

        // 결과 수집
        for (Future<FormattedMatchResponse> future : futures) {
            try {
                FormattedMatchResponse result = future.get();
                if (result != null) {
                    champCount.put(result.getChampionName(), champCount.getOrDefault(result.getChampionName(), 0) + 1);
                    totalKills += result.getKills();
                    totalDeath += result.getDeaths();
                    totalAssists += result.getAssists();

                    if (result.isWin()) {
                        winCount++;
                        winCountMap.put(result.getChampionName(), winCountMap.getOrDefault(result.getChampionName(), 0) + 1);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("current error: {}", e.getMessage());
            }
        }

        executorService.shutdown();

        double winRate = 0;
        if (queueType == QUICK && totalGames > 0) {
            winRate = (double) winCount / totalGames * 100;
        }
        double averageKill = (double) totalKills / totalGames;
        double averageDeath = (double) totalDeath / totalGames;
        double averageAssist = (double) totalAssists / totalGames;

        // accountId, queueType, championName으로 생성된 Favorite이 있으면 업데이트하고, 없으면 새로 만든다.
        for (Map.Entry<String, Integer> entry : champCount.entrySet()) {
            String championName = entry.getKey();
            int playCount = entry.getValue();
            int championWinCount = winCountMap.getOrDefault(championName, 0);

            Favorite existingFavorite = favoriteRepository.findByAccountIdAndQueueTypeAndChampionName(accountId, queueType, championName);

            if (existingFavorite != null) {
                existingFavorite.update(playCount, championWinCount);
            } else {
                Favorite newFavorite = new Favorite(accountId, queueType, championName, playCount, championWinCount);
                favoriteRepository.save(newFavorite);
            }
        }

        return new MatchStats(
                winCount, totalGames - winCount, totalGames, queueType,
                winRate, averageKill, averageDeath, averageAssist
        );
    }
}
