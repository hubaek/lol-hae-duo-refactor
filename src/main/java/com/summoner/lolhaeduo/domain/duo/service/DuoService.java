package com.summoner.lolhaeduo.domain.duo.service;

import com.summoner.lolhaeduo.client.entity.Favorite;
import com.summoner.lolhaeduo.client.repository.FavoriteRepository;
import com.summoner.lolhaeduo.common.dto.AuthMember;
import com.summoner.lolhaeduo.common.dto.PageResponse;
import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.entity.AccountGameData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.FlexRankData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.QuickGameData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.SoloRankData;
import com.summoner.lolhaeduo.domain.account.repository.AccountRepository;
import com.summoner.lolhaeduo.domain.duo.dto.*;
import com.summoner.lolhaeduo.domain.duo.entity.Duo;
import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import com.summoner.lolhaeduo.domain.duo.repository.DuoRepository;
import com.summoner.lolhaeduo.domain.member.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuoService {

    private final DuoRepository duoRepository;
    private final AccountRepository accountRepository;
    private final FavoriteRepository favoriteRepository;

    // 페이징 처리된 듀오 리스트 조회
    public PageResponse<DuoListResponse> getPagedDuoList(QueueType queueType, Lane lane, String tier, int page, int size) {
        // 1. 조건에 맞는 듀오 리스트를 페이징 처리해서 가져오기
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Duo> duoPage = duoRepository.findDuosByCondition(queueType, lane, tier, pageable);

        // 2. 가져온 듀오 리스트를 map()을 사용하여 변환
        Page<DuoListResponse> response = duoPage.map(duo -> {
            // Account 정보 조회
            Account account = accountRepository.findById(duo.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다. accountId=" + duo.getAccountId()));

            List<Long> favoriteIds = favoriteRepository.findTop3ByAccountIdAndQueueTypeOrderByPlayCountDesc(account.getId(), duo.getQueueType())
                    .stream()
                    .map(Favorite::getId)
                    .toList();

            List<String> favoriteChampNames = favoriteIds.stream()
                    .map(favoriteId -> favoriteRepository.findById(favoriteId)
                            .orElseThrow(() -> new IllegalArgumentException("Favorite not found"))
                            .getChampionName())
                    .toList();

            // DuoListResponse 생성
            return DuoListResponse.of(
                duo,
                account.getSummonerName(),
                account.getTagLine(),
                favoriteChampNames
            );
        });
        return  PageResponse.of(response.toList(), pageable , response.getTotalPages());
    }

    @Transactional
    public DuoCreateResponse createDuo(DuoCreateRequest request, Long memberId) {

        Account linkedAccount = accountRepository.findById(request.getAccountId()).orElseThrow(
                () -> new IllegalArgumentException("Not Found Account")
        );

        if (!linkedAccount.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        AccountGameData gameData = linkedAccount.getAccountGameData();
        String profileIconUrl = gameData.getProfileIconIdUrl();
        List<Long> favoriteId = favoriteRepository.findTop3ByAccountIdAndQueueTypeOrderByPlayCountDesc(linkedAccount.getId(), request.getQueueType())
                .stream()
                .map(Favorite::getId)
                .toList();

        Duo duo = createDuoByQueueType(
                request.getQueueType(),
                request,
                gameData,
                profileIconUrl,
                favoriteId,
                linkedAccount
        );

        Duo savedDuo = duoRepository.save(duo);
        int winRate = 0;
        if (savedDuo.getWins() + savedDuo.getLosses() != 0) {
            winRate = savedDuo.getWins() * 100 / (savedDuo.getWins() + savedDuo.getLosses());
        }
        return DuoCreateResponse.of(savedDuo, winRate);
    }

    private Duo createDuoByQueueType(QueueType queueType, DuoCreateRequest request, AccountGameData gameData, String profileIconUrl, List<Long> favoriteId, Account linkedAccount) {
        switch (queueType) {
            case QUICK -> {
                QuickGameData quickGameData = gameData.getQuickGameData();
                return Duo.quickOf(
                        queueType,
                        request.getPrimaryRole(), request.getPrimaryChamp(),
                        request.getSecondaryRole(), request.getSecondaryChamp(),
                        request.getTargetRole(), request.getMemo(), request.getMic(),
                        gameData.getSoloRankData().getTier(),
                        gameData.getSoloRankData().getRanks(),
                        quickGameData.getWins(),
                        quickGameData.getTotalGames() - quickGameData.getWins(),
                        profileIconUrl,
                        quickGameData.getKda(),
                        favoriteId,
                        linkedAccount.getMemberId(), linkedAccount.getId()
                );
            }
            case SOLO -> {
                SoloRankData soloRankData = gameData.getSoloRankData();
                return Duo.soloOf(
                        queueType,
                        request.getPrimaryRole(), request.getTargetRole(),
                        request.getMemo(), request.getMic(),
                        soloRankData.getTier(),
                        soloRankData.getRanks(),
                        soloRankData.getWins(),
                        soloRankData.getTotalGames() - soloRankData.getWins(),
                        profileIconUrl,
                        soloRankData.getKda(),
                        favoriteId,
                        linkedAccount.getMemberId(), linkedAccount.getId()
                );
            }
            case FLEX -> {
                FlexRankData flexRankData = gameData.getFlexRankData();
                return Duo.flexOf(
                        queueType,
                        request.getPrimaryRole(), request.getTargetRole(),
                        request.getMemo(), request.getMic(),
                        flexRankData.getTier(),
                        flexRankData.getRanks(),
                        flexRankData.getWins(),
                        flexRankData.getTotalGames() - flexRankData.getWins(),
                        profileIconUrl,
                        flexRankData.getKda(),
                        favoriteId,
                        linkedAccount.getMemberId(), linkedAccount.getId()
                );
            }
            default -> throw new IllegalArgumentException("지원하지 않는 큐 타입입니다.");
        }
    }


    @Transactional
    public DuoUpdateResponse updateDuo(Long memberId, Long duoId, DuoUpdateRequest request) {
        Duo duo = duoRepository.findById(duoId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 듀오 찾기 글입니다."));
        if (duo.getDeletedAt() != null) {
            throw new IllegalStateException("삭제된 듀오 찾기 글 입니다.");
        }
        if (!duo.getMemberId().equals(memberId)) {
            throw new IllegalStateException("듀오 신청자가 아닙니다.");
        }

        Account linkedAccount = accountRepository.findById(duo.getAccountId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 롤 계정입니다."));
        AccountGameData gameData = linkedAccount.getAccountGameData();
        List<Long> favoriteId = favoriteRepository.findTop3ByAccountIdAndQueueTypeOrderByPlayCountDesc(linkedAccount.getId(), request.getQueueType())
                .stream()
                .map(Favorite::getId)
                .toList();

        if (duo.getQueueType().equals(request.getQueueType())) {
            duo.update(
                    request.getQueueType(),
                    request.getPrimaryRole(),
                    request.getPrimaryChamp(),
                    request.getSecondaryRole(),
                    request.getSecondaryChamp(),
                    request.getTargetRole(),
                    request.getMemo(),
                    request.getMic(),
                    duo.getTier(),
                    duo.getRanks(),
                    duo.getWins(),
                    duo.getLosses(),
                    duo.getKda(),
                    duo.getFavoriteId()
            );
        } else {
            QueueData queueData = collectQueueData(request.getQueueType(), gameData);
            duo.update(
                    request.getQueueType(),
                    request.getPrimaryRole(),
                    request.getPrimaryChamp(),
                    request.getSecondaryRole(),
                    request.getSecondaryChamp(),
                    request.getTargetRole(),
                    request.getMemo(),
                    request.getMic(),
                    queueData.getTier(),
                    queueData.getRanks(),
                    queueData.getWins(),
                    queueData.getLosses(),
                    queueData.getKda(),
                    favoriteId
            );
        }
        return DuoUpdateResponse.of(duo);
    }

    private QueueData collectQueueData(QueueType queueType, AccountGameData gameData) {
        switch (queueType) {
            case QUICK -> {
                QuickGameData quickGameData = gameData.getQuickGameData();
                return QueueData.of(
                        gameData.getSoloRankData().getTier(),
                        gameData.getSoloRankData().getRanks(),
                        quickGameData.getWins(),
                        quickGameData.getTotalGames() - quickGameData.getWins(),
                        quickGameData.getKda()
                );
            }
            case SOLO -> {
                SoloRankData soloRankData = gameData.getSoloRankData();
                return QueueData.of(
                        soloRankData.getTier(),
                        soloRankData.getRanks(),
                        soloRankData.getWins(),
                        soloRankData.getTotalGames() - soloRankData.getWins(),
                        soloRankData.getKda()
                );
            }
            case FLEX -> {
                FlexRankData flexRankData = gameData.getFlexRankData();
                return QueueData.of(
                        flexRankData.getTier(),
                        flexRankData.getRanks(),
                        flexRankData.getWins(),
                        flexRankData.getTotalGames() - flexRankData.getWins(),
                        flexRankData.getKda()
                );
            }
            default -> throw new IllegalArgumentException("지원하지 않는 QueueType입니다.");
        }
    }

    public void deleteDuoById(Long duoId, AuthMember authMember) {
        Duo existDuo = duoRepository.findById(duoId).orElseThrow(
                () -> new IllegalArgumentException("듀오가 존재하지 않습니다.")
        );

        if (!authMember.getMemberId().equals(existDuo.getMemberId())) {
            if (!authMember.getRole().equals(UserRole.ADMIN)) {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }
        }

        if (existDuo.getDeletedAt() != null) {
            throw new IllegalArgumentException("이미 삭제된 듀오입니다.");
        }

        // Soft Delete 처리: 삭제 시간 기록
        existDuo.delete();
        duoRepository.save(existDuo);  // 엔티티를 업데이트하여 삭제 시간 저장
    }
}
