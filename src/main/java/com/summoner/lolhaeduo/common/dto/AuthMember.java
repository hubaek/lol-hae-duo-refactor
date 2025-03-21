package com.summoner.lolhaeduo.common.dto;

import com.summoner.lolhaeduo.domain.member.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMember {
    private final Long memberId;
    private final String username;
    private final UserRole role;
}
