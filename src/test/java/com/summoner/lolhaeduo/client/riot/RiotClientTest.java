//package com.summoner.lolhaeduo.client.riot;
//
//import com.summoner.lolhaeduo.client.dto.FormattedMatchResponse;
//import com.summoner.lolhaeduo.client.dto.LeagueEntryResponse;
//import com.summoner.lolhaeduo.client.dto.PuuidResponse;
//import com.summoner.lolhaeduo.client.dto.SummonerResponse;
//import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
//import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//class RiotClientTest {
//
//    @Autowired
//    private RiotClient riotClient;
//
//    @Test
//    @DisplayName("Extract PUUID using a riot API: SUCCESS")
//    void test1() {
//        // given
//        String summonerName = "진주 갈리오";
//        String tagLine = "KR1";
//        AccountRegion region = AccountRegion.ASIA;
//
//        // when
//        PuuidResponse response = riotClient.extractPuuid(summonerName, tagLine, region);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getPuuid()).isNotEmpty();
//        assertThat(response.getGameName()).isEqualTo(summonerName);
//        assertThat(response.getTagLine()).isEqualTo(tagLine);
//    }
//
//    @Test
//    @DisplayName("Extract summonerInfo using PUUID: SUCCESS")
//    void test2() {
//        // given
//        String puuid = "CY8LOxe0cw7yiD6h2wK5ef4iezjXM4e1ZQyU2-EoF17L5YrWisnjeO4WxOEY3C_4tRa3AP1M3rdsfw";
//        AccountServer server = AccountServer.KR;
//
//        // when
//        SummonerResponse response = riotClient.extractSummonerInfo(puuid, server);
//
//        // then
//        assertThat(response).isNotNull();
//    }
//
//    @Test
//    @DisplayName("Extract LeagueInfo using summonerId: SUCCESS")
//    void test3() {
//        // given
//        String summonerId = "tjEbbL2gjCFL4BTeV-43s2wEzfwTxf5_Ajt6AIRR3_GWzbTcqtW4Rwk2uw";
//        AccountServer server = AccountServer.KR;
//
//        // when
//        List<LeagueEntryResponse> response = riotClient.extractLeagueInfo(summonerId, server);
//
//        // then
//        assertThat(response).isNotNull();
//
//    }
//
//    @Test
//    @DisplayName("Get list of matchIds : SUCCESS")
//    void test4() {
//        // given
//        String puuid = "fV6jIVQldJJRzgUR32GxeFmGcetBzHh7LWPrXceEulG4WXoogx05hi_iOgbUWgbVowD36TtbdvpYsQ";
//        AccountRegion region = AccountRegion.ASIA;
//        Long startTime = 1732172336L;
//        Long endTime = null;
//        Integer queue = null;
//        String type = "ranked";
//        Integer start = 0;
//        Integer count = 5;
//
//        // when
//        List<String> matchIds = riotClient.extractMatchIds(startTime, endTime, queue, type, start, count, region, puuid);
//
//        // then
//        Assertions.assertNotNull(matchIds, "Match Ids list should not be null");
//        Assertions.assertFalse(matchIds.isEmpty(), "Match IDs list should not be empty");
//        System.out.println("Extracted Match IDs: " + matchIds);
//    }
//
//    @Test
//    @DisplayName("Get a match response : SUCCESS")
//    void test5() {
//        // given
//        String matchId = "KR_7402935390";
//        String summonerName = "랭킹 1위 유미";
//        String tagLine = "KR1";
//        AccountRegion region = AccountRegion.ASIA;
//
//        // when
//        FormattedMatchResponse matchDetails = riotClient.getMatchDetails(matchId, summonerName, tagLine, region);
//
//        // then
//        Assertions.assertNotNull(matchDetails, "Match details should not be null");
//        Assertions.assertEquals("Yuumi", matchDetails.getChampionName(), "Champion name should be Yuumi");
//        Assertions.assertEquals(3, matchDetails.getKills(), "Kills should be 3");
//        Assertions.assertEquals(4, matchDetails.getDeaths(), "Deaths should be 4");
//        Assertions.assertEquals(17, matchDetails.getAssists(), "Assists should be 17");
//        Assertions.assertFalse(matchDetails.isWin(), "Win status should be false");
//
//        System.out.println("Match Details: " + matchDetails);
//    }
//}