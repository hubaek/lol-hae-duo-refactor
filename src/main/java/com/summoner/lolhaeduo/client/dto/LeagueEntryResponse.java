package com.summoner.lolhaeduo.client.dto;

import lombok.Getter;

@Getter
public class LeagueEntryResponse {
    String leagueId;
    String summonerId;
    String queueType;
    String tier;
    String rank;
    int leaguePoints;
    int wins;
    int losses;
    boolean hotStreak;
    boolean veteran;
    boolean freshBlood;
    boolean inactive;
}
