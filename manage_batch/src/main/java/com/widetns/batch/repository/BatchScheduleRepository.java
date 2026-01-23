package com.widetns.batch.repository;

import com.widetns.batch.entity.BatchSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchScheduleRepository extends JpaRepository<BatchSchedule, Long> {
    @EntityGraph(attributePaths = "batchInfo")
    List<BatchSchedule> findByEnabled(boolean enabled);
}
