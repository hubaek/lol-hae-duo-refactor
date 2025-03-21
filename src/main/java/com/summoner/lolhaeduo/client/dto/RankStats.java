package com.summoner.lolhaeduo.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankStats {

    private final int soloTotalGames;
    private final int flexTotalGames;

    private final int soloWins;
    private final int soloLosses;
    private final double soloWinRate;
    private final String soloTier;
    private final String soloRank;

    private final int flexWins;
    private final int flexLosses;
    private final double flexWinRate;
    private final String flexTier;
    private final String flexRank;
}
