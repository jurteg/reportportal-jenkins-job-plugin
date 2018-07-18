package com.jurteg.jenkinsci.plugins.reportportal;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

@Extension
public class ReportPortalRunListener extends RunListener<Run> {

    private static final Object EXISTING_LAUNCH_MODEL_MONITOR = new Object();

    @Override
    public void onStarted(Run run, TaskListener listener) {
        try {
            RunProcessor.onStarted(run);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to correctly start '%s'. \\n %s", run.getFullDisplayName(), e.getMessage()));
        }
    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        synchronized (EXISTING_LAUNCH_MODEL_MONITOR) {
            try {
                RunProcessor.onCompleted(run);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Unable to correctly finish '%s'. \\n %s", run.getFullDisplayName(), e.getMessage()));
            }
        }
    }


}
