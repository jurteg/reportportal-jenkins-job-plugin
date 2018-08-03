package com.jurteg.jenkinsci.plugins.reportportal;

import hudson.model.Result;
import hudson.model.Run;

import java.io.IOException;
import java.util.List;

public class RunResult {

    private final static String FAILED = "FAILED";
    private final static String PASSED = "PASSED";
    private final static String INTERRUPTED = "INTERRUPTED";
    private final static int LOG_SIZE_LINES = 100000;

    public static String getStatus(Run run) {
        if (run != null) {
            if (run.getResult() == Result.ABORTED) {
                return INTERRUPTED;
            }
            if (run.getResult() == Result.SUCCESS) {
                return PASSED;
            }
            return FAILED;
        } else {
            return null;
        }
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
                }
            } catch (IOException e) {
                builder.append("Unable to get log: \n").append(e.getMessage());
            }
        }
        return builder.toString();
    }

}
