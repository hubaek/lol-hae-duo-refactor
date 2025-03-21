//package com.summoner.lolhaeduo.domain.duo.service;
//
//import com.summoner.lolhaeduo.common.dto.AuthMember;
//import com.summoner.lolhaeduo.domain.duo.entity.Duo;
//import com.summoner.lolhaeduo.domain.duo.enums.Lane;
//import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
//import com.summoner.lolhaeduo.domain.duo.repository.DuoRepository;
//import com.summoner.lolhaeduo.domain.member.enums.UserRole;
//import jakarta.validation.constraints.AssertTrue;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
//class DuoServiceTest {
//
//    @Autowired
//    private DuoService duoService;
//
//    @Autowired
//    private DuoRepository duoRepository;
//
//    // 1. 삭제가 정상적으로 이루어진 경우 (내가 만든 듀오를 삭제할 경우)
//    @Test
//    @DisplayName("삭제가 정상적으로 이루어진 경우 (내가 만든 듀오를 삭제할 경우)")
//    void test1() {
//        // given : 이러한 조건에서
//        Long memberId = 1L;
//        Duo duo= Duo.rankOf(QueueType.SOLO, Lane.BOTTOM, Lane.SUPPORT, null, false, "123", "123", 1, 1, memberId, 2L);
//        duoRepository.save(duo);
//
//        AuthMember authMember = new AuthMember(memberId, "user1", UserRole.MEMBER);
//
//        // when : 이걸 실행했을 때
//        duoService.deleteDuoById(duo.getId(), authMember);
//
//        // then : 이 값이 나와야한다.
//        Optional<Duo> deletedDuo = duoRepository.findById(duo.getId());
//        Assertions.assertTrue(deletedDuo.isPresent());
//        Assertions.assertNotNull(deletedDuo.get().getDeletedAt(),"deletedAt 필드는 null이어야 합니다");
//    }
//
//    // 2. 삭제가 정상적으로 이루어진 경우 (매니저 권한 멤버가 삭제 요청을 할 경우)
//    @Test
//    @DisplayName("삭제가 정상적으로 이루어진 경우 (매니저가 삭제할 경우)")
//    void test2() {
//        //given
//        Long memberId = 1L;
//        Duo duo = Duo.soloOf(QueueType.SOLO, Lane.BOTTOM, Lane.SUPPORT, null, false, "123", "123", 1, 1, memberId, 2L);
//        duoRepository.save(duo);
//
//        AuthMember authMember = new AuthMember(2L,"user1", UserRole.ADMIN);
//        //when
//        duoService.deleteDuoById(duo.getId(), authMember);
//        //then
//        Optional<Duo> deletedDuo = duoRepository.findById(duo.getId());
//        Assertions.assertTrue(deletedDuo.isPresent());
//        Assertions.assertNotNull(deletedDuo.get().getDeletedAt(), "deletedAt 필드는 null이어야 합니다");
//
//    }
//
//    // 3. 삭제가 정상적으로 이루어지지 않은 경우 (듀오를 만들지 않은 사람이 듀오를 삭제하려고 한 경우)
//    @Test
//    @DisplayName("삭제가 정상적으로 이루어지지 않음(권한없음)")
//    void test3(){
//        //given
//        Long unauthorizedMemberId = 3L;
//        Long memberId = 1L;
//        Duo duo = Duo.rankOf(QueueType.SOLO, Lane.BOTTOM, Lane.SUPPORT, null, false, "123", "123", 1, 1, memberId, 2L);
//        duoRepository.save(duo);
//
//        AuthMember unauthorizedMember = new AuthMember(unauthorizedMemberId, "user2", UserRole.MEMBER);
//
//        //when
//        Assertions.assertThrows(
//                IllegalArgumentException.class, () -> duoService.deleteDuoById(duo.getId(), unauthorizedMember));
//
//        //then
//        Optional<Duo> deletedDuo = duoRepository.findById(duo.getId());
//        Assertions.assertTrue(deletedDuo.isPresent(), "Duo가 삭제되지 않았습니다."); // 데이터가 유지됨을 확인
//
//            }
//}