package com.summoner.lolhaeduo.domain.duo.dto;

import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DuoCreateRequest {

    @NotNull
    private Long accountId;

    @NotNull
    private QueueType queueType;

    @NotNull
    private Lane primaryRole;

    private String primaryChamp;

    private Lane secondaryRole;

    private String secondaryChamp;

    @NotNull
    private Lane targetRole;

    @Size(max = 100, message = "메모는 최대 100자까지만 작성 가능합니다.")
    private String memo;

    @NotNull
    private Boolean mic;

    // 큐 타입에 따른 유효성 검사
    @AssertTrue(message = "큐 타입이 빠른 대전인 경우, primaryChamp 와 secondaryRole, secondaryChamp 는 NULL 이 아니어야 합니다.")
    public boolean isQuickQueueTypeValid() {
        if (queueType != QueueType.QUICK) {
            return primaryChamp == null && secondaryRole == null && secondaryChamp == null;
        }
        return true;
    }
}
