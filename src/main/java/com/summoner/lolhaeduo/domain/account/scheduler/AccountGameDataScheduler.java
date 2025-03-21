package com.summoner.lolhaeduo.domain.account.scheduler;

import com.summoner.lolhaeduo.domain.account.entity.Account;
import com.summoner.lolhaeduo.domain.account.repository.AccountRepository;
import com.summoner.lolhaeduo.domain.account.service.AccountGameDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountGameDataScheduler {

    private final AccountGameDataService accountGameDataService;
    private final AccountRepository accountRepository;

    @Async
    @Scheduled(cron = "0 0 3 * * *")
    public void updateAllAccounts() {
        List<Account> allAccounts = accountRepository.findAll();
        for (Account account : allAccounts) {
            accountGameDataService.updateAccountGameData(account);
        }
    }
}