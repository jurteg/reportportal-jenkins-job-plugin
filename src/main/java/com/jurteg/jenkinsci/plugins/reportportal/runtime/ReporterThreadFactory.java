package com.jurteg.jenkinsci.plugins.reportportal.runtime;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ReporterThreadFactory implements ThreadFactory {

    private static final String NAME_TEMPLATE = "Report Portal thread [%d]";
    private ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable runnable) {
        return builder.setNameFormat(String.format(NAME_TEMPLATE, poolNumber.getAndIncrement())).build().newThread(runnable);
    }
}
