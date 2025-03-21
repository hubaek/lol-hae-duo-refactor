package com.summoner.lolhaeduo.domain.member.controller;

import com.summoner.lolhaeduo.domain.member.dto.LoginRequest;
import com.summoner.lolhaeduo.domain.member.dto.LoginResponse;
import com.summoner.lolhaeduo.domain.member.dto.SignupRequest;
import com.summoner.lolhaeduo.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;
    
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        memberService.signup(signupRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = memberService.login(loginRequest, response);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(loginResponse);
    }
}
