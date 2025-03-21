package com.summoner.lolhaeduo.domain.account.controller;

import com.summoner.lolhaeduo.common.annotation.Auth;
import com.summoner.lolhaeduo.common.dto.AuthMember;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountRequest;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountResponse;
import com.summoner.lolhaeduo.domain.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 반환 값으로 생성된 account의 Id 값을 넘겨준다.
     */
    @PostMapping("/link/riot")
    public ResponseEntity<LinkAccountResponse> linkAccount(@RequestBody @Valid LinkAccountRequest request, @Auth AuthMember authMember) {
        Long memberId = authMember.getMemberId();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.linkAccount(request, memberId));
    }
}
