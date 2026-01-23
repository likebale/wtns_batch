package com.widetns.batch.scheduler;

import com.widetns.batch.entity.BatchHistory;
import com.widetns.batch.entity.BatchInfo;
import com.widetns.batch.entity.BatchRetryHistory;
import com.widetns.batch.entity.BatchSchedule;
import com.widetns.batch.repository.BatchHistoryRepository;
import com.widetns.batch.repository.BatchInfoRepository;
import com.widetns.batch.repository.BatchRetryHistoryRepository;
import com.widetns.batch.repository.BatchScheduleRepository;
import com.widetns.batch.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchInfoRepository batchInfoRepository;
    private final BatchHistoryRepository batchHistoryRepository;
    private final BatchRetryHistoryRepository retryHistoryRepository;
    private final BatchScheduleRepository batchScheduleRepository;
    private final BatchService batchService;
    private final CacheManager cacheManager;

    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행
    public void scheduledBatchExecution() {
        List<BatchSchedule> schedules = batchScheduleRepository.findByEnabled(true);

        for (BatchSchedule schedule : schedules) {
            BatchInfo batch = schedule.getBatchInfo();
            if (batch == null || !batch.getUseYn()) {
                continue;
            }

            if (schedule.getCronExpression() == null || schedule.getCronExpression().isEmpty()) {
                continue;
            }

            try {
                ZoneId zone = schedule.getTimezone() != null && !schedule.getTimezone().isEmpty()
                        ? ZoneId.of(schedule.getTimezone())
                        : ZoneId.systemDefault();

                CronExpression cron = CronExpression.parse(schedule.getCronExpression());
                ZonedDateTime baseTime = batch.getLastExecutionTime() != null
                        ? batch.getLastExecutionTime().atZone(zone)
                        : ZonedDateTime.now(zone).minusMinutes(1);
                ZonedDateTime nextExecution = cron.next(baseTime);

                if (nextExecution != null && nextExecution.isBefore(ZonedDateTime.now(zone))) {
                    log.info("스케줄 배치 실행: {}", batch.getBatchId());
                    batchService.executeBatch(batch.getBatchId(), false);
                }
            } catch (Exception e) {
                log.error("배치 스케줄 확인 실패: {}", batch.getBatchId(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 60000) // 1분마다 재시도 확인
    public void retryFailedBatches() {
        Cache cache = cacheManager.getCache("batchRetry");
        if (cache == null)
            return;

        cache.getNativeCache();

        // 캐시에서 재시도 정보를 순회하며 처리
        // 실제 구현에서는 캐시의 모든 키를 가져와서 처리
        // 여기서는 간략화된 버전
    }

    @SuppressWarnings("unchecked")
    private void processRetry(Map<String, Object> retryInfo) {
        Long historyId = (Long) retryInfo.get("historyId");
        String batchId = (String) retryInfo.get("batchId");
        Integer maxRetryCount = (Integer) retryInfo.get("retryCount");
        Integer currentRetry = (Integer) retryInfo.get("currentRetry");
        Integer retryInterval = (Integer) retryInfo.get("retryInterval");

        if (currentRetry >= maxRetryCount) {
            // 최대 재시도 횟수 초과
            Cache cache = cacheManager.getCache("batchRetry");
            if (cache != null) {
                cache.evict("retry_" + historyId);
            }
            return;
        }

        BatchHistory history = batchHistoryRepository.findById(historyId).orElse(null);
        if (history == null)
            return;

        BatchInfo batchInfo = batchInfoRepository.findByBatchId(batchId).orElse(null);
        if (batchInfo == null)
            return;

        try {
            // 재시도 실행 로직
            BatchRetryHistory retryHistory = new BatchRetryHistory();
            retryHistory.setBatchHistory(history);
            retryHistory.setRetrySequence(currentRetry + 1);
            retryHistory.setRetryTime(LocalDateTime.now());

            // 실제 배치 재실행 로직 (간략화)
            boolean success = true; // 실제로는 batchService.executeBatch 호출

            retryHistory
                    .setStatus(success ? BatchRetryHistory.RetryStatus.SUCCESS : BatchRetryHistory.RetryStatus.FAILED);

            retryHistoryRepository.save(retryHistory);

            if (success) {
                history.setStatus(BatchHistory.ExecutionStatus.SUCCESS);
                batchHistoryRepository.save(history);

                Cache cache = cacheManager.getCache("batchRetry");
                if (cache != null) {
                    cache.evict("retry_" + historyId);
                }
            } else {
                retryInfo.put("currentRetry", currentRetry + 1);
            }

        } catch (Exception e) {
            log.error("배치 재시도 실패: {}", batchId, e);
        }
    }
}
