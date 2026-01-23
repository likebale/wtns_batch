package com.widetns.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_retry_history")
@Getter
@Setter
public class BatchRetryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_history_id", nullable = false)
    private BatchHistory batchHistory;

    @Column(nullable = false)
    private Integer retrySequence;

    @Column(nullable = false)
    private LocalDateTime retryTime;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RetryStatus status;

    @Column(length = 2000)
    private String errorMessage;

    public enum RetryStatus {
        SUCCESS, FAILED
    }
}