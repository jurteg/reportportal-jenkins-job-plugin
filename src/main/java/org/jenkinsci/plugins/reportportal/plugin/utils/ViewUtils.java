package org.jenkinsci.plugins.reportportal.plugin.utils;

import hudson.model.Action;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.reportportal.plugin.view.GeneralView;
import org.jenkinsci.plugins.reportportal.plugin.view.job.DownStreamJobView;
import org.jenkinsci.plugins.reportportal.plugin.view.job.UpStreamJobView;
import org.jenkinsci.plugins.reportportal.plugin.view.LaunchView;
import org.jenkinsci.plugins.reportportal.plugin.view.ReportPortalPlugin;

import java.util.ArrayList;
import java.util.List;

public class ViewUtils {

    public static ReportPortalPlugin getPlugin() {
        List<Action> actions = Jenkins.getInstance().getActions();
        for(Action action : actions) {
            if(action instanceof ReportPortalPlugin) {
                return (ReportPortalPlugin)action;
            }
        }
       throw new RuntimeException("Unable to find existing ReportPortalPlugin. Check if it's installed on Jenkins.");
    }

    public static GeneralView getGeneralView() {
        return getPlugin().getGeneralView();
    }


    public static List<UpStreamJobView> getUpStreamJobViews() {
        List<UpStreamJobView> jobList = new ArrayList<>();
        for(LaunchView launch : getGeneralView().getEntries()) {
            if(launch != null) {
                if(launch.getUpStreamJobView() != null) {
                    jobList.add(launch.getUpStreamJobView());
                }
            }
        }
        return jobList;
    }


    public static List<String> getExistingUpStreamJobNames() {
       return getExistingUpStreamJobNames(getUpStreamJobViews());
    }

    public static List<String> getAllExistingViewNames() {
        List<String> viewNameList = new ArrayList<>();
        //viewNameList.addAll();
        return viewNameList;
    }

    public static List<String> getExistingUpStreamJobNames(List<UpStreamJobView> jobs) {
        List<String> jobNamesList = new ArrayList<>();
        for(UpStreamJobView job : jobs) {
            jobNamesList.add(job.getJobToReportTitle());
        }
        return jobNamesList;
    }

    /*

    public static UpStreamJobView getExistingUpStreamJobByName(String jobName) {
        for(UpStreamJobView existingJob : getUpStreamJobViews()) {
            if(existingJob.getRpTestItemName().equals(jobName)) {
                return existingJob;
            }
        }
        return null;
    }

    */

    public static List<String> getJenkinsJobChildrenNames(String parentJobName) {
        List<String> jobNamesList = new ArrayList<>();
        if (!parentJobName.trim().isEmpty()) {
            for (Item jenkinsJob : Jenkins.getInstance().getItem(parentJobName).getAllJobs()) {
                if (!jenkinsJob.getName().equals(parentJobName)) {
                    jobNamesList.add(jenkinsJob.getName());
                }
            }
        }
        return jobNamesList;
    }

}
