package com.widetns.batch.repository;

import com.widetns.batch.entity.BatchHistory;
import com.widetns.batch.entity.BatchRetryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRetryHistoryRepository extends JpaRepository<BatchRetryHistory, Long> {

    /**
     * 배치 이력에 대한 재시도 이력 조회 (순번순)
     */
    List<BatchRetryHistory> findByBatchHistoryOrderByRetrySequence(BatchHistory batchHistory);

    /**
     * 배치 이력 ID로 재시도 이력 조회
     */
    List<BatchRetryHistory> findByBatchHistory_IdOrderByRetrySequence(Long batchHistoryId);
}
