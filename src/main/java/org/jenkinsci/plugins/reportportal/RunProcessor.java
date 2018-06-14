package org.jenkinsci.plugins.reportportal;

import hudson.model.Run;
import org.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import org.jenkinsci.plugins.reportportal.plugin.utils.ViewUtils;
import org.jenkinsci.plugins.reportportal.plugin.view.Config;

import java.util.ArrayList;
import java.util.List;

public class RunProcessor {


    private static final Object EXISTING_LAUNCH_MODEL_MONITOR = new Object();
    private static List<LaunchModel> runningLaunchModelList = new ArrayList<>();


    public static void onStarted(Run run) {
        if (ViewUtils.getGeneralView() != null) {
            Config config = ViewUtils.getGeneralView().getConfig();
            if (config != null) {
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

    private static void runLaunches(Run run, Config config) {
        List<LaunchModel> newLaunchesToRun = RunUtils.runNewLaunches(run, runningLaunchModelList, config);
        runningLaunchModelList.addAll(newLaunchesToRun);
    }


}
