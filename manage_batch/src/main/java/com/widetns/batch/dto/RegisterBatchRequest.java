package com.widetns.batch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterBatchRequest {
    private String className;
    private String cronExpression;
    private String timezone;
    private Boolean scheduleEnabled;
    private Integer retryCount;
    private Integer retryInterval;
    private Boolean useYn;
}
