package com.jurteg.jenkinsci.plugins.reportportal;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

@Extension
public class ReportPortalRunListener extends RunListener<Run> {

    @Override
    public void onStarted(Run run, TaskListener listener) {
        RunProcessor.onStarted(run, listener);
    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        RunProcessor.onCompleted(run);
    }

}
