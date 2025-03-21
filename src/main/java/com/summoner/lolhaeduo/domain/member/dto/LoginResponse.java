package com.summoner.lolhaeduo.domain.member.dto;

import com.summoner.lolhaeduo.domain.member.enums.UserRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LoginResponse {
    private Long id;
    private String username;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private LoginResponse(Long id, String username, UserRole role, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static LoginResponse of(Long id, String username, UserRole role, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new LoginResponse(id, username, role, createdAt, modifiedAt);
    }
}
