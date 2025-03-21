package com.summoner.lolhaeduo.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotNull(message = "아이디 입력은 필수입니다.")
    private String username;

    @NotNull(message = "비밀번호 입력은 필수입니다.")
    private String password;
}
