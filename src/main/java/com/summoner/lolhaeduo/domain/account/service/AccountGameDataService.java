package com.summoner.lolhaeduo.domain.account.service;

import com.summoner.lolhaeduo.client.dto.MatchStats;
import com.summoner.lolhaeduo.client.dto.RankStats;
import com.summoner.lolhaeduo.client.service.RiotClientService;
import com.summoner.lolhaeduo.common.event.AccountGameDataEvent;
import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.entity.AccountGameData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.FlexRankData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.QuickGameData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.SoloRankData;
import com.summoner.lolhaeduo.domain.account.repository.AccountGameDataRepository;
import com.summoner.lolhaeduo.domain.account.repository.AccountRepository;
import com.summoner.lolhaeduo.domain.account.repository.dataStorage.FlexRankDataRepository;
import com.summoner.lolhaeduo.domain.account.repository.dataStorage.QuickGameDataRepository;
import com.summoner.lolhaeduo.domain.account.repository.dataStorage.SoloRankDataRepository;
import com.summoner.lolhaeduo.domain.duo.entity.Kda;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.summoner.lolhaeduo.domain.duo.enums.QueueType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountGameDataService {

    private final RiotClientService riotClientService;
    private final AccountGameDataRepository accountGameDataRepository;
    private final QuickGameDataRepository quickGameDataRepository;
    private final SoloRankDataRepository soloRankDataRepository;
    private final FlexRankDataRepository flexRankDataRepository;
    private final AccountRepository accountRepository;

    @Async
    @EventListener
    @Transactional
    public void createAccountGameDataEvent(AccountGameDataEvent event) {
        Account account = accountRepository.findById(event.getAccountId()).orElseThrow(
                () -> new IllegalArgumentException("Account not found")
        );
        long startTime = System.currentTimeMillis();

        // 랭크 정보 가져오기
        RankStats rankStats = riotClientService.getRankGameStats(account.getAccountDetail().getEncryptedSummonerId(), account.getServer());

        // 매치 아이디 가져오기
        // 빠른 대전의 경우 플레이한 매치 수 정보를 사용하지 않는다.
        List<String> quickMatchIds = riotClientService.getMatchIds(QUICK, 0, account.getRegion(), account.getAccountDetail().getPuuid());
        List<String> soloMatchIds = riotClientService.getMatchIds(SOLO, rankStats.getSoloTotalGames(), account.getRegion(), account.getAccountDetail().getPuuid());
        List<String> flexMatchIds = riotClientService.getMatchIds(FLEX, rankStats.getFlexTotalGames(), account.getRegion(), account.getAccountDetail().getPuuid());

        QuickGameData quickGameData = null;
        if (!quickMatchIds.isEmpty()) {
            MatchStats quickStats = riotClientService.getMatchStats(account.getId(), quickMatchIds, QUICK, account.getSummonerName(), account.getTagLine(), account.getRegion());
            quickGameData = QuickGameData.of(
                    quickStats.getWins(), quickStats.getTotalGames(),
                    Kda.of(quickStats.getAverageKill(), quickStats.getAverageAssist(), quickStats.getAverageDeath())
            );
        } else {
            quickGameData = QuickGameData.of(0, 0, Kda.of(0,0,0));
        }

        SoloRankData soloRankData = null;
        if (!soloMatchIds.isEmpty()) {
            MatchStats soloStats = riotClientService.getMatchStats(account.getId(), soloMatchIds, SOLO, account.getSummonerName(), account.getTagLine(), account.getRegion());
            soloRankData = SoloRankData.of(
                    rankStats.getSoloTier(), rankStats.getSoloRank(),
                    soloStats.getWins(), soloStats.getTotalGames(),
                    Kda.of(soloStats.getAverageKill(), soloStats.getAverageAssist(), soloStats.getAverageDeath())
            );
        } else {
            soloRankData = SoloRankData.of("UNRANKED", "N/A", 0, 0, Kda.of(0,0,0));
        }

        FlexRankData flexRankData = null;
        if (!flexMatchIds.isEmpty()) {
            MatchStats flexStats = riotClientService.getMatchStats(account.getId(), flexMatchIds, FLEX, account.getSummonerName(), account.getTagLine(), account.getRegion());
            flexRankData = FlexRankData.of(
                    rankStats.getFlexTier(), rankStats.getFlexRank(),
                    flexStats.getWins(), flexStats.getTotalGames(),
                    Kda.of(flexStats.getAverageKill(), flexStats.getAverageAssist(), flexStats.getAverageDeath())
            );
        } else {
            flexRankData = FlexRankData.of("UNRANKED", "N/A", 0, 0, Kda.of(0,0,0));
        }

        // 각각 데이터 객체 생성
        String iconUrl = riotClientService.updateProfileIconUrl(account);

        AccountGameData newAccountGameData = AccountGameData.of(iconUrl, quickGameData, soloRankData, flexRankData);
        long endTime = System.currentTimeMillis();
        log.info("API latency : " + (endTime - startTime));
        account.linkAccountGameData(newAccountGameData);

        accountGameDataRepository.save(newAccountGameData);
    }

    // update를 하기 위해서는 updatedAt을 사용해서 추가 로직이 필요함
    @Transactional
    public void updateAccountGameData(Account account) {
        // 1. AccountGameData를 불러온다.
        AccountGameData recentData = account.getAccountGameData();
        QuickGameData quickGameData = recentData.getQuickGameData();
        SoloRankData soloRankData = recentData.getSoloRankData();
        FlexRankData flexRankData = recentData.getFlexRankData();

        // 2. 가장 최근 업데이트 날짜를 확인한다.
        LocalDateTime modifiedAt = recentData.getModifiedAt();

        // 3. 프로필 아이콘을 업데이트한다.
        String updatedProfileIconUrl = riotClientService.updateProfileIconUrl(account);

        // 4. 반영해야할 매치 ID가 있는지 조회한다.
        RankStats rankStats = riotClientService.getRankGameStats(account.getAccountDetail().getEncryptedSummonerId(), account.getServer());

        List<String> quickMatchIds = riotClientService.updateMatchIds(QUICK, modifiedAt, account.getRegion(), account.getAccountDetail().getPuuid());
        List<String> soloMatchIds = riotClientService.updateMatchIds(SOLO, modifiedAt, account.getRegion(), account.getAccountDetail().getPuuid());
        List<String> flexMatchIds = riotClientService.updateMatchIds(FLEX, modifiedAt, account.getRegion(), account.getAccountDetail().getPuuid());

        if (!quickMatchIds.isEmpty()) {
            MatchStats quickStats = riotClientService.getMatchStats(account.getId(), quickMatchIds, QUICK, account.getSummonerName(), account.getTagLine(), account.getRegion());
            quickGameData.update(
                    quickStats.getWins(), quickStats.getTotalGames(),
                    Kda.of(quickStats.getAverageKill(), quickStats.getAverageAssist(), quickStats.getAverageDeath())
            );
        }

        if (!soloMatchIds.isEmpty()) {
            MatchStats soloStats = riotClientService.getMatchStats(account.getId(), soloMatchIds, SOLO, account.getSummonerName(), account.getTagLine(), account.getRegion());
            soloRankData.update(
                    rankStats.getSoloTier(), rankStats.getSoloRank(),
                    soloRankData.getWins() + soloStats.getWins(),
                    soloRankData.getTotalGames() + soloStats.getTotalGames(),
                    updateKda(soloRankData.getKda(), soloRankData.getTotalGames(), soloStats)
            );
        }

        if (!flexMatchIds.isEmpty()) {
            MatchStats flexStats = riotClientService.getMatchStats(account.getId(), flexMatchIds, FLEX, account.getSummonerName(), account.getTagLine(), account.getRegion());
            flexRankData.update(
                    rankStats.getFlexTier(), rankStats.getFlexRank(),
                    flexRankData.getWins() + flexStats.getWins(),
                    flexRankData.getTotalGames() + flexStats.getTotalGames(),
                    updateKda(flexRankData.getKda(), flexRankData.getTotalGames(), flexStats)
            );
        }

        // 5. AccountGameData 값을 업데이트한다.
        recentData.update(
                updatedProfileIconUrl,
                quickGameDataRepository.save(quickGameData),
                soloRankDataRepository.save(soloRankData),
                flexRankDataRepository.save(flexRankData)
        );
        accountGameDataRepository.save(recentData);
    }

    private Kda updateKda(Kda previousKda, int totalGames, MatchStats matchStats) {
        double totalKill = previousKda.getAverageKills() * totalGames;
        double totalAssist = previousKda.getAverageAssists() * totalGames;
        double totalDeath = previousKda.getAverageDeaths() * totalGames;

        return Kda.of(
                (totalKill + (matchStats.getAverageKill() * matchStats.getTotalGames())) / (totalGames + matchStats.getTotalGames()),
                (totalAssist + (matchStats.getAverageAssist() * matchStats.getTotalGames())) / (totalGames + matchStats.getTotalGames()),
                (totalDeath + (matchStats.getAverageDeath() * matchStats.getTotalGames())) / (totalGames + matchStats.getTotalGames())
        );
    }
}
