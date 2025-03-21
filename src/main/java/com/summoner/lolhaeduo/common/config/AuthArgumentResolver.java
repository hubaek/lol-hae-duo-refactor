package com.summoner.lolhaeduo.common.config;

import com.summoner.lolhaeduo.common.annotation.Auth;
import com.summoner.lolhaeduo.common.dto.AuthMember;
import com.summoner.lolhaeduo.common.util.JwtUtil;
import com.summoner.lolhaeduo.domain.member.enums.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j(topic = "AuthArgumentResolver")
@Component
@AllArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthAnnotation = parameter.getParameterAnnotation(Auth.class) != null;
        boolean isAuthMemberType = parameter.getParameterType().equals(AuthMember.class);

        if (hasAuthAnnotation && !isAuthMemberType) {
            throw new IllegalArgumentException("@Auth 이 있지만 파라미터 타입이 AuthMember 가 아닙니다.");
        }
        if (!hasAuthAnnotation && isAuthMemberType) {
            throw new IllegalArgumentException("@Auth 없이 AuthMember 타입이 요청되었습니다.");
        }
        return true;
    }

    @Override
    public Object resolveArgument(
            @Nullable MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) throws Exception {
        log.info("resolveArgument");
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = request.getHeader("Authorization");
        log.info("Header 에서 읽어 온 토큰: {}", token);

        String jwt = jwtUtil.substringToken(token);
        Claims claims = jwtUtil.getClaimsFromToken(jwt);

        Long memberId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        UserRole role = UserRole.valueOf(claims.get("role", String.class));

        return new AuthMember(memberId, username, role);
    }
}
