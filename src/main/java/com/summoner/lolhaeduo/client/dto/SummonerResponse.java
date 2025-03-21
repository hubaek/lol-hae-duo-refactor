package com.summoner.lolhaeduo.client.dto;

import lombok.Getter;

@Getter
public class SummonerResponse {
    private String accountId;
    private int profileIconId;
    private long revisionDate;
    private String id;
    private String puuid;
    private long summonerLevel;
}
