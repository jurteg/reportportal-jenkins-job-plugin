package org.jenkinsci.plugins.reportportal;

import hudson.model.Run;
import org.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import org.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import org.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import org.jenkinsci.plugins.reportportal.plugin.view.Config;

import java.util.ArrayList;
import java.util.List;

public class RunUtils {

    public static void runExistingDownstreamJobsOrCreateSiblings(Run run, List<LaunchModel> launchModelListToRunFrom) {
        for (JobModel jobModel : ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToRunFrom, getRunJobName(run))) {
            if (jobModel.getRpTestItemId() != null) { //running
                //if (!jobModel.getRun().getFullDisplayName().equals(run.getFullDisplayName()) && ModelUtils.hasSameParentByRunName(jobModel, run)) {
                    if (!jobModel.getRun().getFullDisplayName().equals(run.getFullDisplayName()) && ModelUtils.haveSameParent(jobModel, run)) {
                    //DownStreamJobModel newJobModel = new DownStreamJobModel(jobModel);//need to create tree of downstreams
                  try {
                      DownStreamJobModel newJobModel = (DownStreamJobModel)((DownStreamJobModel) jobModel).clone();
                      ((JobModel) jobModel.getParent()).addDownStreamJobModel(newJobModel);
                      if (newJobModel.getRpTestItemName().equals(jobModel.getRpTestItemName())) {
                          newJobModel.setRpTestItemName(newJobModel.getRpTestItemName() + String.format("[%d]", ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToRunFrom, getRunJobName(run)).size()));
                      }
                      newJobModel.start(run);
                      String dfdf = "";
                  } catch (CloneNotSupportedException e) {

                  }
                }
            } else {
                jobModel.start(run);
                String sdad = "";
            }
        }
    }

    public static void finishRunningDownStreamJobs(Run run, List<LaunchModel> launchModelListToRunFrom) {
        for (JobModel jobModel : ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToRunFrom, getRunJobName(run))) {
            if (jobModel.getRpTestItemId() != null) { //running

                if(run.equals(jobModel.getRun())){
                //if (ModelUtils.hasSameParentByRunName(jobModel, run)) {
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
        for(LaunchModel model : ModelUtils.getLaunchModelListToRun(run)) {
            if (!isAlreadyRunningInList(model, runningLaunchesList)) {
                if(model.isReportingEnabled()) {
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
