package com.widetns.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "batch_history")
@Getter
@Setter
public class BatchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExecutionType executionType;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(length = 2000)
    private String errorMessage;

    @OneToMany(mappedBy = "batchHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchRetryHistory> retryHistories = new ArrayList<>();

    public enum ExecutionType {
        SCHEDULED, MANUAL
    }

    public enum ExecutionStatus {
        RUNNING, SUCCESS, FAILED
    }
}
