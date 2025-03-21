package com.summoner.lolhaeduo.domain.duo.dto;

import com.summoner.lolhaeduo.domain.duo.entity.Kda;
import lombok.Getter;

@Getter
public class QueueData {
    private String tier;
    private String ranks;
    private int wins;
    private int losses;
    private Kda kda;

    private QueueData(String tier, String ranks, int wins, int losses, Kda kda) {
        this.tier = tier;
        this.ranks = ranks;
        this.wins = wins;
        this.losses = losses;
        this.kda = kda;
    }

    public static QueueData of(String tier, String ranks, int wins, int losses, Kda kda) {
        return new QueueData(tier, ranks, wins, losses, kda);
    }
}
