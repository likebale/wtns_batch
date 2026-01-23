package com.widetns.batch.sample;

import com.widetns.batch.core.BatchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleHelloBatch implements BatchJob {
    private static final Logger log = LoggerFactory.getLogger(SampleHelloBatch.class);

    @Override
    public boolean execute() {
        log.info("SampleHelloBatch executed.");
        return true;
    }

    @Override
    public String getBatchId() {
        return "SAMPLE_HELLO";
    }

    @Override
    public String getBatchName() {
        return "Sample Hello Batch";
    }

    @Override
    public String getBatchDescription() {
        return "Sample batch job that logs a hello message.";
    }
}
