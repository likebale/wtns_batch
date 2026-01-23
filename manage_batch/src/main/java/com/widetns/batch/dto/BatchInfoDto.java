package com.widetns.batch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchInfoDto {
    private String batchName;
    private String batchDescription;
    private String cronExpression;
    private Integer retryCount;
    private Integer retryInterval;
    private Boolean useYn;
}
