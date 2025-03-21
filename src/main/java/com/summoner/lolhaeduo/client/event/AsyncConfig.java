package com.summoner.lolhaeduo.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int cpuCoreCount = Runtime.getRuntime().availableProcessors();      // 서버의 CPU 코어 수 확인
//        log.info("[확인] cpuCoreCount = {}", cpuCoreCount);

        executor.setCorePoolSize(cpuCoreCount * 2);                         // Thread Pool 에서 기본적으로 유지되는 스레드의 개수
        executor.setThreadNamePrefix("custom-");                            // 생성되는 스레드 이름 커스텀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // 작업 큐가 가득 찬 상태에서 작업이 추가될 때 발생하는 예외
        executor.initialize();
        return executor;
    }
}
