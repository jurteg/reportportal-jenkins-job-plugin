package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.LaunchView;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.List;

public class ModelUtils {

    public static boolean haveSameParent(JobModel jobModel, Run run) {
        if (jobModel.getParent() != null) {
            AbstractProject parentProject = ((Build) run).getProject().getBuildingUpstream();
            if (parentProject == null) {
                throw new IllegalStateException(String.format("'%s' job should be downstream, but it has no parent (upstream job). " +
                        "Please check Multijob dependencies on plugin configuration page.", run.getFullDisplayName()));
            }
            return parentProject.getLastBuild() == ((JobModel) jobModel.getParent()).getRun();
        }
        return false;
    }

    public static List<LaunchModel> getLaunchModelListToRun(Run run, ConfigModel config) {
        List<LaunchModel> launchModelList = new ArrayList<>();
        for (LaunchView launchView : ViewUtils.getLaunchViewListForRun(run)) {
            LaunchModel launchModel = new LaunchModel(launchView, run, config);
            launchModelList.add(launchModel);
        }
        return launchModelList;
    }

    public static List<LaunchModel> getExistingLaunchesToFinish(Run run, List<LaunchModel> runningLaunchesList) {
        List<LaunchModel> launchesToFinish = new ArrayList<>();
        for (LaunchModel model : runningLaunchesList) {
            if (model.getUpstreamJobModel().getRun().equals(run)) {
                launchesToFinish.add(model);
            }
        }
        return launchesToFinish;
    }

    public static List<JobModel> getDownStreamJobsFromModelsByName(List<LaunchModel> launchModelList, String nameToMatch) {
        List<JobModel> nameMatchedJobs = new ArrayList<>();
        for (LaunchModel launchModel : launchModelList) {
            for (JobModel jobModel : launchModel.getUpstreamJobModel().getDownStreamJobModelList()) {
                List<JobModel> intermediateJobModelList = new ArrayList<>();
                if(nameToMatch.equals(jobModel.getJobName())) {
                    intermediateJobModelList.add(jobModel);
                }
                nameMatchedJobs.addAll(getDownstreamJobFromJobModelTree(jobModel, nameToMatch, intermediateJobModelList));
            }
        }
        return nameMatchedJobs;
    }

    public static List<JobModel> getDownstreamJobFromJobModelTree(JobModel jobModelTree, String name,  List<JobModel> jobModelList) {
        if (jobModelTree.getDownStreamJobModelList() != null && !jobModelTree.getDownStreamJobModelList().isEmpty()) {
            for (JobModel jobModel : jobModelTree.getDownStreamJobModelList()) {
                if (name.equals(jobModel.getJobName())) {
                    jobModelList.add(jobModel);
                }
                getDownstreamJobFromJobModelTree(jobModel, name, jobModelList);
            }
        }
        return jobModelList;
    }

}
