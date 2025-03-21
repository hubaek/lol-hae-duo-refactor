package com.summoner.lolhaeduo.domain.duo.dto;

import com.summoner.lolhaeduo.domain.duo.entity.Duo;
import com.summoner.lolhaeduo.domain.duo.entity.Kda;
import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class DuoUpdateResponse {
    private Long duoId;                 // 듀오 찾기의 ID
    private QueueType queueType;        // 큐 타입
    private Lane primaryRole;           // 주 역할군
    private String primaryChamp;        // 주 역할군의 선호 챔피언
    private Lane secondaryRole;         // 부 역할군
    private String secondaryChamp;      // 부 역할군의 선호 챔피언
    private Lane targetRole;            // 선호하는 매칭 역할군
    private String memo;                // 메모
    private Boolean mic;                // 마이크
    private String tier;                // 신청한 유저의 티어
    private String ranks;               // 신청한 유저의 랭크
    private int wins;
    private int losses;
    private List<Long> favoritesChamp;
    private String profileIcon;
    private Kda kda;
    private Long memberId;              // 신청한 유저의 ID
    private Long accountId;             // 신청한 유저의 연동 계정 ID
    private LocalDateTime createdAt;     // 듀오 찾기 생성 일자
    private LocalDateTime modifiedAt;   // 듀오 찾기 수정 일자

    private DuoUpdateResponse(Duo duo) {
        this.duoId = duo.getId();
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
        this.wins = duo.getWins();
        this.losses = duo.getLosses();
        this.favoritesChamp = duo.getFavoriteId();
        this.profileIcon = duo.getProfileIcon();
        this.kda = duo.getKda();
        this.memberId = duo.getMemberId();
        this.accountId = duo.getAccountId();
        this.createdAt = duo.getCreatedAt();
        this.modifiedAt = duo.getModifiedAt();
    }

    public static DuoUpdateResponse of(Duo duo) {
        return new DuoUpdateResponse(duo);
    }
}
