package com.summoner.lolhaeduo.domain.duo.dto;

import com.summoner.lolhaeduo.domain.duo.entity.Duo;
import com.summoner.lolhaeduo.domain.duo.entity.Kda;
import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class DuoListResponse {
  private Long id;
  private String profileIconId;
  private String summonerName;
  private String tagLine;
  private QueueType queueType;
  private String tier;
  private String rank;
  private int winRate;
  private Kda kda;
  private List<String> favorites;
  private Lane primaryRole;
  private Lane targetRole;
  private String primaryChamp;
  private Lane secondaryRole;
  private String secondaryChamp;
  private String memo;
  private boolean mic;
  private Long memberId;
  private Long accountId;
  private String relativeTime;


  private DuoListResponse(Duo duo, String summonerName, String tagLine, List<String> favorites) {
    this.id = duo.getId();
    this.profileIconId = duo.getProfileIcon();
    this.summonerName = summonerName;
    this.tagLine = tagLine;
    this.queueType = duo.getQueueType();
    this.tier = duo.getTier();
    this.rank = duo.getRanks();
    try {
      this.winRate = duo.getWins() * 100 / (duo.getWins() + duo.getLosses());
    } catch (ArithmeticException e) {
      this.winRate = 0;
    }
    this.mic = duo.getMic();
    this.kda = duo.getKda();
    this.favorites = favorites;
    this.primaryRole = duo.getPrimaryRole();
    this.targetRole = duo.getTargetRole();
    this.primaryChamp = duo.getPrimaryChamp();
    this.secondaryRole = duo.getSecondaryRole();
    this.secondaryChamp = duo.getSecondaryChamp();
    this.memo = duo.getMemo();
    this.memberId = duo.getMemberId();
    this.accountId = duo.getAccountId();
    this.relativeTime = calculateRelativeTime(duo.getCreatedAt());
  }

  public static DuoListResponse of(Duo duo, String summonerName, String tagLine, List<String> favorites){
    return new DuoListResponse(duo, summonerName, tagLine, favorites);
  }

  public String calculateRelativeTime(LocalDateTime createdAt) {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.between(createdAt, now);

    long seconds = duration.getSeconds();
    long minutes = duration.toMinutes();
    long hours = duration.toHours();
    long days = duration.toDays();

    if (seconds < 60) {
      return seconds + "초 전";
    } else if (minutes < 60) {
      return minutes + "분 전";
    } else if (hours < 24) {
      return hours + "시간 전";
    } else {
      return days + "일 전";
    }
  }
}