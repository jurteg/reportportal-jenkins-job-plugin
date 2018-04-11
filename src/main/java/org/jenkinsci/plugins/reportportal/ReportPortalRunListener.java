package org.jenkinsci.plugins.reportportal;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.reportportal.plugin.ReportPortalPlugin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

@Extension
public class ReportPortalRunListener extends RunListener<Run> {

    private final static String FAILED = "FAILED";
    private final static String PASSED = "PASSED";
    private final static int LOG_SIZE_LINES = 100000;

    private boolean isRunning = false;
    private ReportPortalPlugin plugin;
    private LaunchReporter reporter;

    @Override
    public void onStarted(Run run, TaskListener listener) {
        plugin = (ReportPortalPlugin) Jenkins.getInstance().getPluginManager().getPlugin(ReportPortalPlugin.class).getPlugin();
        if (plugin.isEnableReporting()) {
            if (plugin.getJobName().equals(run.getFullDisplayName().replace(run.getSearchName(), "").trim())) {
                reporter = new LaunchReporter(plugin.getSettings());
                reporter.beforeLaunch();
                isRunning = true;
            }

        }
    }

    @Override
    public void onCompleted(Run build, @Nonnull TaskListener listener) {
        if (isRunning) {
            reporter.afterLaunch(getResult(build), getLog(build));
        }
    }

    private String getResult(Run build) {
        if (build.getResult() == Result.SUCCESS) {
            return PASSED;
        }
        return FAILED;
    }

    @SuppressWarnings("unchecked")
    private String getLog(Run build) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> log = build.getLog(LOG_SIZE_LINES);
            if (!log.isEmpty()) {
                for (String string : log) {
                    builder.append(string).append("\n");
                }
            }
        } catch (IOException e) {
            builder.append("Unable to get log: \n").append(e.getMessage());
        }
        return builder.toString();
    }

}
