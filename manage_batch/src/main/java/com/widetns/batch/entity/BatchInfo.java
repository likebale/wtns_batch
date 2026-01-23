package com.widetns.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_info")
@Getter
@Setter
public class BatchInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String batchId;

    @Column(nullable = false, length = 200)
    private String batchName;

    @Column(length = 1000)
    private String batchDescription;

    @Column(nullable = false, length = 500)
    private String className;

    @Column(length = 100)
    private String cronExpression;

    @Column(nullable = false)
    private Integer retryCount = 3;

    @Column(nullable = false)
    private Integer retryInterval = 5;

    @Column(nullable = false)
    private Boolean useYn = true;

    private LocalDateTime lastExecutionTime;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
