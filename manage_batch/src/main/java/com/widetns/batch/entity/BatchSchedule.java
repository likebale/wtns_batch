package com.widetns.batch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_schedule")
@Getter
@Setter
public class BatchSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_info_id", nullable = false, unique = true)
    @JsonIgnore
    private BatchInfo batchInfo;

    @Column(length = 100)
    private String cronExpression;

    @Column(length = 50)
    private String timezone;

    @Column(nullable = false)
    private Boolean enabled = false;

    private LocalDateTime nextRunAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
