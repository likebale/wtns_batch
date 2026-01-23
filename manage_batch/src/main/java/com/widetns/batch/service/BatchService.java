package com.widetns.batch.service;

import com.widetns.batch.core.BatchJob;
import com.widetns.batch.entity.BatchHistory;
import com.widetns.batch.entity.BatchInfo;
import com.widetns.batch.repository.BatchHistoryRepository;
import com.widetns.batch.repository.BatchInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchInfoRepository batchInfoRepository;
    private final BatchHistoryRepository batchHistoryRepository;
    private final CacheManager cacheManager;
    private final String BATCH_LIB_PATH = "./batch-libs/";

    @Transactional
    public List<String> scanUnregisteredBatches() throws Exception {
        List<String> unregisteredBatches = new ArrayList<>();
        File libDir = new File(BATCH_LIB_PATH);

        if (!libDir.exists() || !libDir.isDirectory()) {
            return unregisteredBatches;
        }

        File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null)
            return unregisteredBatches;

        for (File jarFile : jarFiles) {
            List<String> batchClasses = findBatchClasses(jarFile);
            for (String className : batchClasses) {
                if (!batchInfoRepository.existsByClassName(className)) {
                    unregisteredBatches.add(className);
                }
            }
        }

        return unregisteredBatches;
    }

    private List<String> findBatchClasses(File jarFile) throws Exception {
        List<String> batchClasses = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile);
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[] { jarFile.toURI().toURL() },
                        this.getClass().getClassLoader())) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (BatchJob.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            batchClasses.add(className);
                        }
                    } catch (Exception e) {
                        log.debug("Failed to load class: {}", className);
                    }
                }
            }
        }

        return batchClasses;
    }

    @Transactional
    public BatchInfo registerBatch(String className) throws Exception {
        BatchJob batchJob = loadBatchJob(className);

        BatchInfo batchInfo = new BatchInfo();
        batchInfo.setBatchId(batchJob.getBatchId());
        batchInfo.setBatchName(batchJob.getBatchName());
        batchInfo.setBatchDescription(batchJob.getBatchDescription());
        batchInfo.setClassName(className);

        return batchInfoRepository.save(batchInfo);
    }

    @Transactional
    public void executeBatch(String batchId, boolean isManual) {
        BatchInfo batchInfo = batchInfoRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("배치 정보를 찾을 수 없습니다."));

        if (!batchInfo.getUseYn()) {
            throw new RuntimeException("사용 중지된 배치입니다.");
        }

        BatchHistory history = new BatchHistory();
        history.setBatchId(batchId);
        history.setExecutionType(isManual ? BatchHistory.ExecutionType.MANUAL : BatchHistory.ExecutionType.SCHEDULED);
        history.setStatus(BatchHistory.ExecutionStatus.RUNNING);
        history.setStartTime(LocalDateTime.now());
        history = batchHistoryRepository.save(history);

        try {
            BatchJob batchJob = loadBatchJob(batchInfo.getClassName());
            boolean success = batchJob.execute();

            history.setStatus(success ? BatchHistory.ExecutionStatus.SUCCESS : BatchHistory.ExecutionStatus.FAILED);
            history.setEndTime(LocalDateTime.now());
            batchHistoryRepository.save(history);

            batchInfo.setLastExecutionTime(LocalDateTime.now());
            batchInfoRepository.save(batchInfo);

            // 스케줄 실행이고 실패한 경우 재시도 캐시에 추가
            if (!isManual && !success) {
                cacheRetryInfo(history.getId(), batchInfo);
            }

        } catch (Exception e) {
            log.error("배치 실행 실패: {}", batchId, e);
            history.setStatus(BatchHistory.ExecutionStatus.FAILED);
            history.setEndTime(LocalDateTime.now());
            history.setErrorMessage(e.getMessage());
            batchHistoryRepository.save(history);

            if (!isManual) {
                cacheRetryInfo(history.getId(), batchInfo);
            }
        }
    }

    private void cacheRetryInfo(Long historyId, BatchInfo batchInfo) {
        Cache cache = cacheManager.getCache("batchRetry");
        if (cache != null) {
            Map<String, Object> retryInfo = new HashMap<>();
            retryInfo.put("historyId", historyId);
            retryInfo.put("batchId", batchInfo.getBatchId());
            retryInfo.put("retryCount", batchInfo.getRetryCount());
            retryInfo.put("retryInterval", batchInfo.getRetryInterval());
            retryInfo.put("currentRetry", 0);

            cache.put("retry_" + historyId, retryInfo);
        }
    }

    private BatchJob loadBatchJob(String className) throws Exception {
        File libDir = new File(BATCH_LIB_PATH);
        File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles == null) {
            throw new RuntimeException("배치 라이브러리를 찾을 수 없습니다.");
        }

        URL[] urls = Arrays.stream(jarFiles)
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);

        URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        Class<?> clazz = classLoader.loadClass(className);
        return (BatchJob) clazz.getDeclaredConstructor().newInstance();
    }
}
