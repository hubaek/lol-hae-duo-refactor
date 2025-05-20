package com.summoner.lolhaeduo.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectMapperConfig {

    /**
     * 어플리케이션의 기본 ObjectMapper 설정
     * 다른 설정 클래스에서 활용할 수 있는 공통 설정
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 기본 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        // 기본 설정
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    /**
     * 타입 정보가 필요한 객체 직렬화를 위한 ObjectMapper 설정
     * Redis와 같이 다형성이 필요한 경우 사용
     */
    @Bean(name = "typedObjectMapper")
    public ObjectMapper typedObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 기본 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        // 기본 설정
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 다형성 타입 검증기 설정
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("com.summoner.lolhaeduo")
                .allowIfSubType("java.util")
                .allowIfSubType("com.summoner.lolhaeduo.domain.account.enums")
                .build();

        // 타입 정보 포함 설정
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return objectMapper;
    }
}
