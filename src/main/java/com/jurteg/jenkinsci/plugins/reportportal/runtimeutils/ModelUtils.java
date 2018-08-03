package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.Context;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.LaunchView;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ModelUtils {

    private static final String MULTIJOB_ACTION = "com.tikal.jenkins.plugins.multijob.MultiJobBuilder$MultiJobAction";

    public static boolean haveTheSameParent(JobModel jobModel, Run run) {
        if (jobModel.getParent() != null && ((JobModel)jobModel.getParent()).getRun() != null) {
            int buildNumber = getParentBuildNumber(run);
            if(buildNumber == -1) {
                throw new IllegalStateException(String.format("Something went wrong when retrieving build number from run: '%s'", run.getFullDisplayName()));
            }
            return buildNumber == ((JobModel)jobModel.getParent()).getRun().getNumber();
        }
        return false;
    }

    public static <T extends Action> T filterAction(List<? extends Action> actionList, String clazz) {
        for(Action action : actionList) {
            if(clazz.equals(action.getClass().getName())) {
                return (T)action;
            }
        }
        return null;
    }

    public static int getParentBuildNumber(Run run) {
        Action action = filterAction(run.getAllActions(), MULTIJOB_ACTION);
        if (action == null) {
            throw new IllegalStateException(String.format("'%s' job should be downstream, but it has no parent (upstream job). " +
                    "Please check Multijob dependencies on plugin configuration page.", run.getFullDisplayName()));
        }
        return getBuildNumber(action);
    }

    public static int getBuildNumber(Action action) {
        int buildNumber = -1;
        try {
            Field buildNumberField = action.getClass().getField("buildNumber");
            boolean isAccessible = buildNumberField.isAccessible();
            if(!isAccessible) {
                buildNumberField.setAccessible(true);
            }
            buildNumber = buildNumberField.getInt(action);
            buildNumberField.setAccessible(isAccessible);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return buildNumber;
    }

    public static List<LaunchModel> getLaunchModelListToRun(Run run, TaskListener listener, ConfigModel config) {
        List<LaunchModel> launchModelList = new ArrayList<>();
        for (LaunchView launchView : ViewUtils.getLaunchViewListForRun(run)) {
            LaunchModel launchModel = new LaunchModel(launchView, run, listener, config);
            launchModelList.add(launchModel);
        }
        return launchModelList;
    }

    public static List<LaunchModel> getExistingLaunchesToFinish(Run run) {
        List<LaunchModel> launchesToFinish = new ArrayList<>();
        for (LaunchModel model : Context.getRunningLaunches()) {
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
                if (nameToMatch.equals(jobModel.getJobName())) {
                    intermediateJobModelList.add(jobModel);
                }
                nameMatchedJobs.addAll(getDownstreamJobFromJobModelTree(jobModel, nameToMatch, intermediateJobModelList));
            }
        }
        return nameMatchedJobs;
    }

    public static List<JobModel> getDownstreamJobFromJobModelTree(JobModel jobModelTree, String name, List<JobModel> jobModelList) {
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
