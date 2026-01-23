package com.widetns.batch.repository;

import com.widetns.batch.entity.BatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchHistoryRepository extends JpaRepository<BatchHistory, Long> {

    /**
     * 재시도 이력을 포함하여 모든 배치 이력 조회 (페이징)
     */
    @Query("SELECT DISTINCT h FROM BatchHistory h " +
            "LEFT JOIN FETCH h.retryHistories " +
            "ORDER BY h.startTime DESC")
    Page<BatchHistory> findAllWithRetries(Pageable pageable);

    /**
     * ID로 재시도 이력을 포함하여 조회
     */
    @Query("SELECT h FROM BatchHistory h " +
            "LEFT JOIN FETCH h.retryHistories " +
            "WHERE h.id = :id")
    Optional<BatchHistory> findByIdWithRetries(@Param("id") Long id);

    /**
     * 배치 ID로 이력 조회 (최신순)
     */
    List<BatchHistory> findByBatchIdOrderByStartTimeDesc(String batchId);
}
