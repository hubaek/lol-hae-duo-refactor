package com.summoner.lolhaeduo.client.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ParticipantsResponse {
    private final String puuid;
    private final int assists;
    private final String championName;
    private final int deaths;
    private final int kills;
    private final boolean win;
}
