package com.summoner.lolhaeduo.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountGameDataEvent {
    private final Long accountId;
}
