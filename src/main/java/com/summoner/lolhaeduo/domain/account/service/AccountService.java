package com.summoner.lolhaeduo.domain.account.service;

import com.summoner.lolhaeduo.client.service.RiotClientService;
import com.summoner.lolhaeduo.common.event.AccountGameDataEvent;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountRequest;
import com.summoner.lolhaeduo.domain.account.dto.LinkAccountResponse;
import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.entity.AccountDetail;
import com.summoner.lolhaeduo.domain.account.repository.AccountRepository;
import com.summoner.lolhaeduo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.summoner.lolhaeduo.domain.account.enums.AccountType.RIOT;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final RiotClientService riotClientService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * link account (for now, members can only link with RIOT account)
     * @param request accountType, accountId, accountPassword, summonerName, tagLine, server
     * @param memberId from token
     */
    public LinkAccountResponse linkAccount(LinkAccountRequest request, Long memberId) {
        memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Not Found Member"));

        if (accountRepository.isMemberAccountLimitExceeded(memberId)) {
            throw new IllegalArgumentException("Account limit(3) exceeded");
        }

        if (accountRepository.existsBySummonerNameAndTagLine(request.getSummonerName(), request.getTagLine())) {
            throw new IllegalArgumentException("Account has been linked by another member");
        }

        if (request.getAccountType().equals(RIOT)) {
            AccountDetail newAccountDetail = riotClientService.createAccountDetail(request);

            Account newAccount = Account.of(
                    request.getAccountUsername(),
                    request.getAccountPassword(),
                    RIOT,
                    request.getSummonerName(),
                    request.getTagLine(),
                    request.getServer(),
                    newAccountDetail,
                    memberId
            );

            accountRepository.save(newAccount);

            // 이벤트 발생
            eventPublisher.publishEvent(new AccountGameDataEvent(newAccount.getId()));

            return LinkAccountResponse.of(newAccount.getId());
        }
        return null;
    }
}
