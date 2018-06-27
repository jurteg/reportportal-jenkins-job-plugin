package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.Config;
import hudson.model.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RunUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(RunUtils.class);

    public static void runExistingDownstreamJobsOrCreateSiblings(Run run, List<LaunchModel> launchModelListToRunFrom) {
        List<JobModel> existingModels = ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToRunFrom, getRunJobName(run));
        int instanceCounter = existingModels.size();
        for (JobModel jobModel : existingModels) {
            if (jobModel.getRpTestItemId() != null) { //is running
                if (!jobModel.getRun().getFullDisplayName().equals(run.getFullDisplayName()) && ModelUtils.haveSameParent(jobModel, run)) {
                    try {
                        DownStreamJobModel newJobModel = (DownStreamJobModel) ((DownStreamJobModel) jobModel).clone();
                        ((JobModel) jobModel.getParent()).addDownStreamJobModel(newJobModel);
                        if (newJobModel.getRpTestItemName().equals(jobModel.getRpTestItemName())) {
                            int tempCounter = instanceCounter - (instanceCounter - 1);
                            newJobModel.setRpTestItemName(newJobModel.getRpTestItemName() + String.format("[%d]", tempCounter));
                            instanceCounter -= tempCounter;
                        }
                        newJobModel.start(run);
                    } catch (CloneNotSupportedException e) {
                        LOGGER.error(String.format("Unable to clone test item with name '%s'.", jobModel.getRpTestItemId()));
                    }
                }
            } else {
                jobModel.start(run);
            }
        }
    }

    public static void finishRunningDownStreamJobs(Run run, List<LaunchModel> launchModelListToRunFrom) {
        for (JobModel jobModel : ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToRunFrom, getRunJobName(run))) {
            if (jobModel.getRpTestItemId() != null) { //running
                if (run.equals(jobModel.getRun())) {
                    jobModel.finish(run);
                }
            }
        }
    }

    public static void finishRunningLaunches(Run run, List<LaunchModel> launchesToFinish) {
        for (LaunchModel model : launchesToFinish) {
            model.getUpstreamJobModel().finish(run);
            model.finish();
        }
    }

    public static List<LaunchModel> runNewLaunches(Run run, List<LaunchModel> runningLaunchesList, Config config) {
        List<LaunchModel> newLaunchesToRun = new ArrayList<>();
        for (LaunchModel model : ModelUtils.getLaunchModelListToRun(run)) {
            if (!isAlreadyRunningInList(model, runningLaunchesList)) {
                if (model.isReportingEnabled()) {
                    model.start(run, config);
                    newLaunchesToRun.add(model);
                }
            }
        }
        return newLaunchesToRun;
    }

    public static String getRunJobName(Run run) {
        return run.getFullDisplayName().replace(run.getSearchName(), "").trim();
    }

    public static boolean isAlreadyRunningInList(LaunchModel launchModel, List<LaunchModel> launchModelListToCompareWith) {
        for (LaunchModel model : launchModelListToCompareWith) {
            if (model.equals(launchModel)) {
                return true;
            }
        }
        return false;
    }

}
