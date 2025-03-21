package com.summoner.lolhaeduo.domain.duo.dto;

import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DuoUpdateRequest {
    @NotNull
    private QueueType queueType;    // 큐 타입

    @NotNull
    private Lane primaryRole;       // 주 역할군

    private String primaryChamp;    // 주 역할군의 선호 챔피언

    private Lane secondaryRole;     // 부 역할군

    private String secondaryChamp;  // 부 역할군의 선호 챔피언

    @NotNull
    private Lane targetRole;  // 선호하는 매칭 역할군

    @Size(max = 50, message = "메모는 최대 50자까지만 작성 가능합니다.")
    private String memo;            // 메모

    @NotNull
    private Boolean mic;            // 마이크

    // 큐 타입에 따른 유효성 검사
    @AssertTrue(message = "큐 타입이 빠른 대전인 경우, primaryChamp 와 secondaryRole, secondaryChamp 는 NULL 이 아니어야 합니다.")
    public boolean isQuickQueueTypeValid() {
        if (queueType != QueueType.QUICK) {
            return primaryChamp == null && secondaryRole == null && secondaryChamp == null;
        }
        return true;
    }

    private DuoUpdateRequest(QueueType queueType, Lane primaryRole, String primaryChamp, Lane secondaryRole, String secondaryChamp, Lane targetRole, String memo, Boolean mic) {
        this.queueType = queueType;
        this.primaryRole = primaryRole;
        this.primaryChamp = primaryChamp;
        this.secondaryRole = secondaryRole;
        this.secondaryChamp = secondaryChamp;
        this.targetRole = targetRole;
        this.memo = memo;
        this.mic = mic;
    }

    public static DuoUpdateRequest of(QueueType queueType, Lane primaryRole, String primaryChamp, Lane secondaryRole, String secondaryChamp, Lane targetRole, String memo, Boolean mic) {
        return new DuoUpdateRequest(queueType, primaryRole, primaryChamp, secondaryRole, secondaryChamp, targetRole, memo, mic);
    }
}
