package com.summoner.lolhaeduo.client.riot;

import com.summoner.lolhaeduo.client.entity.Champion;
import com.summoner.lolhaeduo.client.entity.ProfileIcon;
import com.summoner.lolhaeduo.client.entity.Version;
import com.summoner.lolhaeduo.client.repository.ChampionRepository;
import com.summoner.lolhaeduo.client.repository.ProfileIconRepository;
import com.summoner.lolhaeduo.client.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataDragonScheduler {

    private final VersionRepository versionRepository;
    private final ChampionRepository championRepository;
    private final ProfileIconRepository profileIconRepository;
    private final RestTemplate restTemplate;

    @Scheduled(cron = "0 0 0 * * MON")  // Update at every monday 00:00
    public void updateDataDragonInfo() {
        String latestVersion = fetchLatestVersion();
        saveVersion(latestVersion);
        updateChampions(latestVersion);
        updateProfileIcons(latestVersion);
    }

    private String fetchLatestVersion() {
        String url = "https://ddragon.leagueoflegends.com/api/versions.json";
        ResponseEntity<List<String>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {}
        );
        return response.getBody().get(0);
    }

    private void saveVersion(String version) {
        Version latestVersion = Version.of(version);
        versionRepository.save(latestVersion);
    }

    private void updateChampions(String version) {
        String url = String.format(
                "https://ddragon.leagueoflegends.com/cdn/%s/data/ko_KR/champion.json",
                version
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        // Delete original data
        championRepository.deleteAll();

        List<Champion> champions = data.values().stream()
            .map(champData -> {
                Map<String, Object> champMap = (Map<String, Object>) champData;
                return Champion.of(
                    champMap.get("id").toString(),
                    champMap.get("name").toString()
                );
            })
            .toList();

        championRepository.saveAll(champions);
    }

    private void updateProfileIcons(String version) {
        String url = String.format(
            "https://ddragon.leagueoflegends.com/cdn/%s/data/ko_KR/profileicon.json",
            version
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        // Delete original data
        profileIconRepository.deleteAll();

        List<ProfileIcon> icons = data.values().stream()
            .map(iconData -> {
                Map<String, Object> iconMap = (Map<String, Object>) iconData;
                return ProfileIcon.of(
                        Integer.parseInt(iconMap.get("id").toString())
                );
            })
            .toList();

        profileIconRepository.saveAll(icons);
    }
}
