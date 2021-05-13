package com.jurteg.jenkinsci.plugins.reportportal;

import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.LogLevel;
import hudson.model.Cause;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rp.com.google.common.collect.ImmutableMap;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RunResult {

    private static Logger LOGGER = LoggerFactory.getLogger(RunResult.class);
    private final static String FAILED = "FAILED";
    private final static String PASSED = "PASSED";
    private final static String INTERRUPTED = "INTERRUPTED";
    private final static int LOG_SIZE_LINES = 100000;

    //@formatter:off
    public static final Map<Result, ItemStatus> STATUS_MAPPING = ImmutableMap.<Result, ItemStatus>builder()
            .put(Result.SUCCESS, ItemStatus.PASSED)
            .put(Result.FAILURE, ItemStatus.FAILED)
            .put(Result.ABORTED, ItemStatus.STOPPED)
            .put(Result.NOT_BUILT, ItemStatus.INTERRUPTED)
            .put(Result.UNSTABLE, ItemStatus.FAILED).build();

    public static final Map<Result, LogLevel> LOG_LEVEL_MAPPING = ImmutableMap.<Result, LogLevel>builder()
            .put(Result.SUCCESS, LogLevel.INFO)
            .put(Result.FAILURE, LogLevel.ERROR)
            .put(Result.ABORTED, LogLevel.WARN)
            .put(Result.NOT_BUILT, LogLevel.WARN)
            .put(Result.UNSTABLE, LogLevel.WARN).build();
    //@formatter:on



    public static ItemStatus getStatus(Run run) {
        if (run != null) {
            if (STATUS_MAPPING.get(run.getResult()) != null) {
                return STATUS_MAPPING.get(run.getResult());
            } else {
                return ItemStatus.FAILED;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static String getLog(Run run) {
        StringBuilder builder = new StringBuilder();
        if (run != null) {
            try {
                List<String> log = run.getLog(LOG_SIZE_LINES);
                if (!log.isEmpty()) {
                    for (String string : log) {
                        builder.append(string).append("\n");
                    }
                    LOGGER.warn(String.format("Log is empty for job %s. Getting causes...", run.getFullDisplayName()));
                    StringWriter writer = new StringWriter();
                    run.getLogText().writeLogTo(0, writer);
                    builder.append("Causes:\n");
                    for(Object cause : run.getCauses()) {
                        builder.append(((Cause)cause).getShortDescription()).append("\n");
                    }
                    builder.append("More logs:\n");
                    builder.append(writer.toString());
                } else {
                    LOGGER.warn(String.format("Log is empty for job %s. Getting causes...", run.getFullDisplayName()));
                    StringWriter writer = new StringWriter();
                    run.getLogText().writeLogTo(0, writer);
                    builder.append("Causes:\n");
                    for(Object cause : run.getCauses()) {
                        builder.append(((Cause)cause).getShortDescription()).append("\n");
                    }
                    builder.append("More logs:\n");
                    builder.append(writer.toString());
                }
            } catch (IOException e) {
                builder.append("Unable to get log: \n").append(e.getMessage());
            }
        }
        return builder.toString();
    }

    /**
     * Map Cucumber statuses to RP log levels
     *
     * @param result - Run result
     */
    public static String mapLevel(Result result) {
        if (LOG_LEVEL_MAPPING.get(result) != null) {
            return LOG_LEVEL_MAPPING.get(result).name();
        }
        return LOG_LEVEL_MAPPING.get(Result.FAILURE).name();

//        String mapped = null;
//        if ("passed".equalsIgnoreCase(status)) {
//            mapped = "info";
//        } else if ("skipped".equalsIgnoreCase(status)) {
//            mapped = "warn";
//        } else {
//            mapped = "error";
//        }
//        return mapped;
    }

}
