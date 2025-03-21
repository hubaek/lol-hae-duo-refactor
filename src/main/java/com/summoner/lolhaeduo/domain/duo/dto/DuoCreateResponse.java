package com.summoner.lolhaeduo.domain.duo.dto;

import com.summoner.lolhaeduo.domain.duo.entity.Duo;
import com.summoner.lolhaeduo.domain.duo.entity.Kda;
import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import lombok.Getter;

import java.util.List;

@Getter
public class DuoCreateResponse {

    private Long id;
    private QueueType queueType;
    private Lane primaryRole;
    private String primaryChamp;
    private Lane secondaryRole;
    private String secondaryChamp;
    private Lane targetRole;
    private String memo;
    private Boolean mic;
    private String tier;
    private String ranks;
    private Kda kda;
    private List<Long> favoriteId;
    private int wins;
    private int losses;
    private String profileIcon;
    private Long memberId;
    private Long accountId;
    private int winRate;

    private DuoCreateResponse(Duo duo, int winRate) {
        this.id = duo.getId();
        this.queueType = duo.getQueueType();
        this.primaryRole = duo.getPrimaryRole();
        this.primaryChamp = duo.getPrimaryChamp();
        this.secondaryRole = duo.getSecondaryRole();
        this.secondaryChamp = duo.getSecondaryChamp();
        this.targetRole = duo.getTargetRole();
        this.memo = duo.getMemo();
        this.mic = duo.getMic();
        this.tier = duo.getTier();
        this.ranks = duo.getRanks();
        this.kda = duo.getKda();
        this.favoriteId = duo.getFavoriteId();
        this.wins = duo.getWins();
        this.losses = duo.getLosses();
        this.profileIcon = duo.getProfileIcon();
        this.memberId = duo.getMemberId();
        this.accountId = duo.getAccountId();
        this.winRate = winRate;
    }

    public static DuoCreateResponse of(Duo duo, int winRate) {
        return new DuoCreateResponse(duo, winRate);
    }
}
