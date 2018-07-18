package com.jurteg.jenkinsci.plugins.reportportal;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.utils.UiUtils;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.ConfigView;
import com.jurteg.jenkinsci.plugins.reportportal.runtimeutils.ModelUtils;
import com.jurteg.jenkinsci.plugins.reportportal.runtimeutils.RunUtils;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RunProcessor {


    private static final Object EXISTING_LAUNCH_MODEL_MONITOR = new Object();
    private static List<LaunchModel> runningLaunchModelList = Collections.synchronizedList(new ArrayList<>());


    public static void onStarted(Run run) {
        if (UiUtils.getGeneralView() != null) {
            ConfigView configView = UiUtils.getGeneralView().getConfig();
            if (configView != null) {
                ConfigModel config = new ConfigModel(configView);
                synchronized (EXISTING_LAUNCH_MODEL_MONITOR) {
                    RunUtils.runExistingDownstreamJobsOrCreateSiblings(run, runningLaunchModelList);
                    runLaunches(run, config);
                }
            }
        }
    }

    public static void onCompleted(Run run) {
        synchronized (EXISTING_LAUNCH_MODEL_MONITOR) {
            RunUtils.finishRunningDownStreamJobs(run, runningLaunchModelList);
            finishRunningLaunches(run);
        }
    }

    private static void finishRunningLaunches(Run run) {
        List<LaunchModel> launchesToFinish = ModelUtils.getExistingLaunchesToFinish(run, runningLaunchModelList);
        RunUtils.finishRunningLaunches(run, launchesToFinish);
        runningLaunchModelList.removeAll(launchesToFinish);
    }

    private static void runLaunches(Run run, ConfigModel config) {
        List<LaunchModel> newLaunchesToRun = RunUtils.runNewLaunches(run, runningLaunchModelList, config);
        runningLaunchModelList.addAll(newLaunchesToRun);
    }


}
