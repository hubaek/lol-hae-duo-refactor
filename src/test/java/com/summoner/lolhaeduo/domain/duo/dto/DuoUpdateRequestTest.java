//package com.summoner.lolhaeduo.domain.duo.dto;
//
//import com.summoner.lolhaeduo.domain.duo.enums.Lane;
//import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.Validation;
//import jakarta.validation.Validator;
//import jakarta.validation.ValidatorFactory;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class DuoUpdateRequestTest {
//
//    private Validator validator;
//
//    @BeforeEach
//    void setUp() {
//        // Validator 초기화
//        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//        validator = factory.getValidator();
//    }
//
//    @Test
//    @DisplayName("유효한 DuoUpdateRequest 가 검증을 통과한다.")
//    void validDuoUpdateRequest() {
//        // Given
//        DuoUpdateRequest request = DuoUpdateRequest.of(
//                QueueType.QUICK,
//                Lane.TOP,
//                "Teemo",
//                Lane.JUNGLE,
//                "Shaco",
//                Lane.MID,
//                "즐겜합시다",
//                true
//        );
//
//        // When
//        Set<ConstraintViolation<DuoUpdateRequest>> violations = validator.validate(request);
//
//        // Then
//        assertTrue(violations.isEmpty(), "유효성 검증이 통과해야 합니다.");
//    }
//
//    @Test
//    @DisplayName("Flex 큐 타입에서 조건 위반 시 검증에 실패한다.")
//    void flexQueueTypeValidationFails() {
//        // Given
//        DuoUpdateRequest request = DuoUpdateRequest.of(
//                QueueType.FLEX,
//                Lane.TOP,
//                "Teemo",
//                Lane.JUNGLE,
//                "Shaco",
//                Lane.MID,
//                "즐겜합시다",
//                true
//        );
//
//        // When
//        Set<ConstraintViolation<DuoUpdateRequest>> violations = validator.validate(request);
//
//        // Then
//        assertEquals(1, violations.size(), "검증 위반이 1건이어야 합니다.");
//        assertEquals("큐 타입이 빠른 대전인 경우, primaryChamp 와 secondaryRole, secondaryChamp 는 NULL 이어야 합니다.",
//                violations.iterator().next().getMessage());
//    }
//}