package com.widetns.batch.repository;

import com.widetns.batch.entity.BatchInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchInfoRepository extends JpaRepository<BatchInfo, Long> {

    /**
     * 배치 ID로 조회
     */
    Optional<BatchInfo> findByBatchId(String batchId);

    /**
     * 클래스명 존재 여부 확인
     */
    boolean existsByClassName(String className);

    /**
     * 사용 여부로 조회
     */
    List<BatchInfo> findByUseYn(Boolean useYn);

    @EntityGraph(attributePaths = "schedule")
    Page<BatchInfo> findAll(Pageable pageable);
}
