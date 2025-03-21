package com.summoner.lolhaeduo.client.dto;

import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchStats {

    private final int wins;
    private final int losses;
    private final int totalGames;
    private final QueueType queueType;

    private final double winRate;
    private final double averageKill;
    private final double averageDeath;
    private final double averageAssist;
}
