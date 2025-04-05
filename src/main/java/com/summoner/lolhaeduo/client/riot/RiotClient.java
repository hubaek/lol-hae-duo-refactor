package com.summoner.lolhaeduo.client.riot;

import com.summoner.lolhaeduo.client.dto.*;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RiotClient {

    private final RestTemplate restTemplate;
    private final MeterRegistry meterRegistry;

    private final Map<String, String> regionBaseUrls = Map.of(
            "ASIA", "https://asia.api.riotgames.com",
            "AMERICAS", "https://americas.api.riotgames.com",
            "EUROPE", "https://europe.api.riotgames.com"
    );

    @Value("${riot.api.key}")
    private String apiKey;

    public RiotClient(RestTemplate restTemplate, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.meterRegistry = meterRegistry;

        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            long startTime = System.currentTimeMillis();
            try {
                var response = execution.execute(request, body);

                meterRegistry.timer("riot_api_response_time",
                                "uri", request.getURI().getPath(),
                                "status", String.valueOf(response.getStatusCode().value()))
                        .record(System.currentTimeMillis() - startTime, java.util.concurrent.TimeUnit.MILLISECONDS);

                String rateLimitRemaining = response.getHeaders().getFirst("X-Rate-Limit-Remaining");
                if (rateLimitRemaining != null && Integer.parseInt(rateLimitRemaining) == 0) {
                    meterRegistry.counter("riot_api_rate_limit_reached",
                                    "uri", request.getURI().getPath())
                            .increment();
                }

                return response;
            } catch (Exception e) {
                meterRegistry.counter("riot_api_errors",
                                "uri", request.getURI().getPath(),
                                "error", e.getClass().getSimpleName())
                        .increment();
                throw e;
            }
        });
    }

    // todo : refactor baseUrl 매직스트링(상수)으로 만들기
    public PuuidResponse extractPuuid(String summonerName, String tagLine, AccountRegion region) {
        String baseUrl = regionBaseUrls.getOrDefault(region.toString(), null);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid region specified: " + region);
        }

        String url = String.format(
                "%s/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s",
                baseUrl, summonerName, tagLine, apiKey
        );

        return restTemplate.getForObject(url, PuuidResponse.class);
    }

    // todo : refactor serverDomain 매직스트링(상수)으로 만들기
    public SummonerResponse extractSummonerInfo(String puuid, AccountServer server) {
        String serverDomain = server.name().toLowerCase();

        String url = String.format(
                "https://%s.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s",
                serverDomain, puuid, apiKey
        );

        return restTemplate.getForObject(url, SummonerResponse.class);
    }

    public List<LeagueEntryResponse> extractLeagueInfo(String summonerId, AccountServer server) {
        String serverDomain = server.name().toLowerCase();

        String url = String.format(
                "https://%s.api.riotgames.com/lol/league/v4/entries/by-summoner/%s?api_key=%s",
                serverDomain, summonerId, apiKey
        );
        try {
            LeagueEntryResponse[] responseArray = restTemplate.getForObject(url, LeagueEntryResponse[].class);
            if (responseArray == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(responseArray));
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch league info from API", e);
        }
    }

    public List<String> extractMatchIds(Long startTime, Long endTime, Integer queue, String type,
                                        Integer start, Integer count, AccountRegion region, String puuid) {
        String baseUrl = regionBaseUrls.getOrDefault(region.toString(), null);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid region specified: " + region);
        }

        StringBuilder urlBuilder = new StringBuilder(
                String.format("%s/lol/match/v5/matches/by-puuid/%s/ids?", baseUrl, puuid)
        );

        if (startTime != null) {
            urlBuilder.append("startTime=").append(startTime).append("&");
        }
        if (endTime != null) {
            urlBuilder.append("endTime=").append(endTime).append("&");
        }
        if (queue != null) {
            urlBuilder.append("queue=").append(queue).append("&");
        }
        if (type != null && !type.isEmpty()) {
            urlBuilder.append("type=").append(type).append("&");
        }
        if (start != null) {
            urlBuilder.append("start=").append(start).append("&");
        } else {
            urlBuilder.append("start=0&");
        }
        if (count != null) {
            urlBuilder.append("count=").append(count).append("&");
        } else {
            urlBuilder.append("count=20&");
        }

        urlBuilder.append("api_key=").append(apiKey);

        String url = urlBuilder.toString();

        ResponseEntity<List<String>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    @Retryable(
            retryFor = { RestClientException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000)
    )
    public FormattedMatchResponse getMatchDetails(String matchId, String summonerName, String tagLine, AccountRegion region) {
        String baseUrl = regionBaseUrls.getOrDefault(region.toString(), null);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid region specified: " + region);
        }

        String url = String.format(
                "%s/lol/match/v5/matches/%s?api_key=%s",
                baseUrl, matchId, apiKey
        );

        MatchResponse matchResponse = restTemplate.getForObject(url, MatchResponse.class);
        if (matchResponse == null || matchResponse.getInfo() == null || matchResponse.getInfo().getParticipants() == null) {
            throw new IllegalArgumentException("Invalid match specified: " + matchId);
        }

        return matchResponse.getInfo().getParticipants().stream()
                .filter(p -> summonerName.equals(p.getRiotIdGameName()) && tagLine.equals(p.getRiotIdTagline()))
                .map(target -> new FormattedMatchResponse(
                        target.getChampionName(),
                        target.getKills(),
                        target.getDeaths(),
                        target.getAssists(),
                        target.isWin()
                ))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("No matching participant found in the match")
                );

    }

    @Recover
    public FormattedMatchResponse recover(RuntimeException e, String matchId, String summonerName, String tagLine, AccountRegion region) {
        log.warn("재시도 실패: matchId={}, summoner={}, tagLine={}, region={}, 이유={}",
                matchId, summonerName, tagLine, region, e.getMessage());
        return null;
    }
}
