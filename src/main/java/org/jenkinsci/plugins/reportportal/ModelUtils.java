package org.jenkinsci.plugins.reportportal;

import hudson.model.Build;
import hudson.model.Run;
import org.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import org.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import org.jenkinsci.plugins.reportportal.plugin.view.LaunchView;

import java.util.ArrayList;
import java.util.List;

public class ModelUtils {

    public static boolean haveSameParent(JobModel jobModel, Run run) {
        if(jobModel.getParent() != null) {
            return (((Build) run).getProject().getBuildingUpstream().getBuildingDownstream().getNextBuildNumber()) - (((JobModel)jobModel.getParent()).getRun().getNumber()) == 1;
            //return run.getParent().getFullDisplayName().equals(((JobModel)jobModel.getParent()).getRun().getFullDisplayName());
        }
        return false;
    }

    public static List<LaunchModel> getLaunchModelListToRun(Run run) {
        List<LaunchModel> launchModelList = new ArrayList<>();
        for (LaunchView launchView : ViewUtils.getLaunchViewListForRun(run)) {
            LaunchModel launchModel = new LaunchModel(launchView, run);
            //launchModel.setRun(run);//wrong, should set run only for appropriate model
            launchModelList.add(launchModel);
        }
        //нужно добавлять ран в модель, только если джоба является апстримной. Для даунстрима всё равно нужно добавлять, только в соответствующую модель
        return launchModelList;
    }

    public static List<LaunchModel> getExistingLaunchesToFinish(Run run, List<LaunchModel> runningLaunchesList) {
        List<LaunchModel> launchesToFinish = new ArrayList<>();
        for (LaunchModel model : runningLaunchesList) {
            if (model.getUpstreamJobModel().getRun().getFullDisplayName().equals(run.getFullDisplayName())) {
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