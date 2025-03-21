package com.summoner.lolhaeduo.domain.member.service;

import com.summoner.lolhaeduo.common.config.PasswordEncoder;
import com.summoner.lolhaeduo.common.util.JwtUtil;
import com.summoner.lolhaeduo.domain.member.dto.LoginRequest;
import com.summoner.lolhaeduo.domain.member.dto.LoginResponse;
import com.summoner.lolhaeduo.domain.member.dto.SignupRequest;
import com.summoner.lolhaeduo.domain.member.entity.Member;
import com.summoner.lolhaeduo.domain.member.enums.UserRole;
import com.summoner.lolhaeduo.domain.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final UserRole memberRole = UserRole.MEMBER;

    public void signup(SignupRequest signupRequest) {
        boolean isExist = memberRepository.existsByUsername(signupRequest.getUsername());
        if (isExist) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        if (!signupRequest.getPassword().equals(signupRequest.getPasswordConfirmation())) {
            throw new IllegalArgumentException("입력하신 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
        String encodePassword = passwordEncoder.encode(signupRequest.getPassword());

        Member member = Member.of(signupRequest.getUsername(), encodePassword, memberRole);
        memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (member.getDeletedAt() != null) {
            throw new IllegalStateException("탈퇴한 사용자입니다.");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new IllegalStateException("비밀번호가 틀립니다.");
        }

        String token = jwtUtil.createToken(member.getId(), member.getUsername(), memberRole);
        response.setHeader("Authorization", token);

        return LoginResponse.of(member.getId(), member.getUsername(), member.getRole(), member.getCreatedAt(), member.getModifiedAt());
    }
}
