package com.summoner.lolhaeduo.common.filter;

import com.summoner.lolhaeduo.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j(topic = "JwtFilter")
@Component
@Order(1)
@AllArgsConstructor
public class JwtFilter extends HttpFilter {

    private final JwtUtil jwtUtil;

    @Override
    public void doFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String url = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();

        log.info("필터 통과하는 url: {}", url);
        if ( StringUtils.hasText(url) &&
                ( url.equals("/")
                        || url.startsWith("/signup")
                        || url.startsWith("/login")
                        || ( url.startsWith("/duo") && method.equals("GET") )
                )
        ) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        String bearerToken = httpServletRequest.getHeader("Authorization");
        log.info("Bearer 토큰: {}", bearerToken);

        String token = jwtUtil.substringToken(bearerToken);
        log.info("토큰 = {}", token);

        jwtUtil.validateToken(token);

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }
}
