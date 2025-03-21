package com.summoner.lolhaeduo.domain.account.entity;

import com.summoner.lolhaeduo.common.entity.Timestamped;
import com.summoner.lolhaeduo.domain.account.enums.AccountRegion;
import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import com.summoner.lolhaeduo.domain.account.enums.AccountType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
public class Account extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private AccountType accountType;

	@Column(nullable = false)
	private String summonerName;

	@Column(nullable = false)
	private String tagLine;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountRegion region;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountServer server;

	@Embedded
	private AccountDetail accountDetail;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private AccountGameData accountGameData;

	@Column(nullable = false)
	private Long memberId;

	private Account(String username, String password, AccountType accountType, String summonerName, String tagLine, AccountServer server, AccountDetail accountDetail, Long memberId) {
		this.username = username;
		this.password = password;
		this.accountType = accountType;
		this.summonerName = summonerName;
		this.tagLine = tagLine;
		this.server = server;
		this.region = server.getRegion();
		this.accountDetail = accountDetail;
		this.memberId = memberId;
	}

	public static Account of(String username, String password, AccountType accountType, String summonerName, String tagLine, AccountServer server, AccountDetail accountDetail,  Long memberId) {
		return new Account(username, password, accountType, summonerName, tagLine, server, accountDetail, memberId);
	}

	// AccountGameData 연동하는 메서드
	public void linkAccountGameData(AccountGameData accountGameData) {
		this.accountGameData = accountGameData;
	}
}
