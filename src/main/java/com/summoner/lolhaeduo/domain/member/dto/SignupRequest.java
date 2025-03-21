package com.summoner.lolhaeduo.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {
    @NotNull(message = "아이디 입력은 필수입니다.")
    @Size(min = 3, message = "아이디는 3자 이상으로 설정해야 합니다.")
    @Size(max = 100, message = "아이디는 100자 이하로 설정해야 합니다.")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,100}$", message = "아이디는 문자, 숫자, 밑줄만 허용합니다.")
    private String username;

    @NotNull(message = "비밀번호 입력은 필수입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상, 30자 이하로 설정해야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,30}$",
            message = "비밀번호는 하나 이상의 문자, 숫자 및 특수 문자(!@#$%^&*(),.?\":{}|<>)를 포함해야 합니다.")
    private String password;

    @NotNull(message = "비밀번호 확인 입력은 필수입니다.")
    @Size(min = 8, max = 30, message = "확인 비밀번호는 8자 이상, 30자 이하로 설정해야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,30}$",
            message = "확인 비밀번호는 하나 이상의 문자, 숫자 및 특수 문자(!@#$%^&*(),.?\":{}|<>)를 포함해야 합니다.")
    private String passwordConfirmation;
}
