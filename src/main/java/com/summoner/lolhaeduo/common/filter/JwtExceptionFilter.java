package com.summoner.lolhaeduo.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summoner.lolhaeduo.common.dto.FilterExceptionResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JwtExceptionFilter")
@Component
@Order(0)
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // 응답이 이미 커밋되었으면 아무 작업하지 않음
            if (servletResponse.isCommitted()) {
                return;
            }
            // 오류 응답 설정
            setErrorResponse(servletResponse, e);
        }
    }

    private static void setErrorResponse(HttpServletResponse servletResponse, Exception e) throws IOException {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        servletResponse.setStatus(httpStatus.value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("utf-8");

        String json = new ObjectMapper().writeValueAsString(new FilterExceptionResponse(httpStatus, e.getMessage()));
        servletResponse.getWriter().write(json);
    }
}