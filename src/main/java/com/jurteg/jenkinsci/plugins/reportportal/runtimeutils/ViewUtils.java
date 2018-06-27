package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.utils.UiUtils;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.JobView;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.LaunchView;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.List;

public class ViewUtils {

    public static List<LaunchView> getLaunchViewListForRun(Run run) {
        List<LaunchView> launchViewList = new ArrayList<>();
        for (LaunchView launchView : UiUtils.getGeneralView().getEntries()) {
            if (launchView.getUpStreamJobView().getJobToReportTitle().equals(RunUtils.getRunJobName(run))) {
                launchViewList.add(launchView);
            }
        }
        return launchViewList;
    }


    public static List<JobView> getDownstreamJobFromJobViewTree(JobView jobViewTree, String name, List<JobView> intermediateList) {
        if (jobViewTree.getDownStreamJobView() != null && !jobViewTree.getDownStreamJobView().isEmpty()) {
            for (JobView jobView : jobViewTree.getDownStreamJobView()) {
                if (name.equals(jobView.getJobToReportTitle())) {
                    intermediateList.add(jobView);
                }
                getDownstreamJobFromJobViewTree(jobView, name, intermediateList);
            }
        }
        if (name.equals(jobViewTree.getJobToReportTitle())) {
            intermediateList.add(jobViewTree);
        }
        return intermediateList;
    }

}
