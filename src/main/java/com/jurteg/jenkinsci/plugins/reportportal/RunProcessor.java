package com.jurteg.jenkinsci.plugins.reportportal;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.utils.UiUtils;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.ConfigView;
import com.jurteg.jenkinsci.plugins.reportportal.runtimeutils.ModelUtils;
import com.jurteg.jenkinsci.plugins.reportportal.runtimeutils.RunUtils;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.util.List;


public class RunProcessor {


    private static final Object EXISTING_LAUNCH_MODEL_MONITOR = new Object();


    public static void onStarted(Run run, TaskListener listener) {
        if (UiUtils.getGeneralView() != null) {
            ConfigView configView = UiUtils.getGeneralView().getConfig();
            //if (configView != null) {
                ConfigModel config = new ConfigModel(configView);
                synchronized (EXISTING_LAUNCH_MODEL_MONITOR) {
                    RunUtils.runExistingDownstreamJobsOrCreateSiblings(run, listener);
                    runLaunches(run, listener, config);
                }
            //}
        }
    }

    public static void onCompleted(Run run) {
        synchronized (EXISTING_LAUNCH_MODEL_MONITOR) {
            RunUtils.finishRunningDownStreamJobs(run);
            finishRunningLaunches(run);
        }
    }

    private static void finishRunningLaunches(Run run) {
        List<LaunchModel> launchesToFinish = ModelUtils.getExistingLaunchesToFinish(run);
        RunUtils.finishRunningLaunches(run, launchesToFinish);
        Context.removeRunningLaunches(launchesToFinish);
    }

    private static void runLaunches(Run run, TaskListener listener, ConfigModel config) {
        List<LaunchModel> newLaunchesToRun = RunUtils.runNewLaunches(run, listener, config);
        Context.addRunningLaunches(newLaunchesToRun);
    }


}
