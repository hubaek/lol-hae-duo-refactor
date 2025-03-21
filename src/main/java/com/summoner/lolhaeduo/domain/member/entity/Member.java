package com.summoner.lolhaeduo.domain.member.entity;

import java.time.LocalDateTime;

import com.summoner.lolhaeduo.common.entity.Timestamped;
import com.summoner.lolhaeduo.domain.member.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member")
public class Member extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private UserRole role;

	private LocalDateTime deletedAt;

	private Member(String username, String password, UserRole role) {
		this.username = username;
		this.password = password;
		this.role = role;
	}

	public static Member of(String username, String password, UserRole role) {
		return new Member(username, password, role);
	}
}
