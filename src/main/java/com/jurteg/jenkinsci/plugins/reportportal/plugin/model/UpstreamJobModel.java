package com.jurteg.jenkinsci.plugins.reportportal.plugin.model;

import com.jurteg.jenkinsci.plugins.reportportal.Context;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.UpStreamJobView;
import hudson.model.Run;

import java.util.Objects;
import java.util.Optional;

public class UpstreamJobModel extends AbstractJobModel {

    private boolean isSuspending;

    public UpstreamJobModel(UpStreamJobView view, ParentAware parent, Run run) {
        this.jobName = view.getJobToReportTitle();
        this.description = view.getDescription();
        this.tags = view.getTags();
        this.rpTestItemName = view.getRpTestItemName();
        this.downStreamJobModelList = view.createDownStreamJobModelList(this);
        this.parent = parent;
        this.run = run;
        resolveBaseName();
    }

    public boolean isSuspending() {
        return isSuspending;
    }

    public void isSuspending(boolean isSuspending) {
        this.isSuspending = isSuspending;
    }

    @Override
    public String getTestItemType() {
        return "SUITE";
    }

    @Override
    public void start() {
        if (rpTestItemId != null) {
            throw new IllegalStateException("Attempting to start already running RP item: " + rpTestItemId.blockingGet());
        }
        if (((LaunchModel) getParent()).getLaunch() == null) {
            throw new IllegalStateException(String.format("Unable to run RP item for job model '%s' because parent item '%s' is not running.", toString(), parent.toString()));
        }
        rpTestItemId = Context.launchUtils().startTestItem(((LaunchModel) parent).getLaunch(), null, getComposedName(), getComposedDescription(description), processTags(tags), getTestItemType(), Optional.of(run.getFullDisplayName()));
        startLogItem();
    }

    @Override
    public String toString() {
        return "Job name: " + jobName + " Description: " + description + " Parent name: " + ((LaunchModel) parent).getName() + " Run full name: " + run.getFullDisplayName();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        UpstreamJobModel other = (UpstreamJobModel) obj;
        return Objects.equals(jobName, other.jobName) &&
                Objects.equals(description, other.description) &&
                Objects.equals(tags, other.tags) &&
                Objects.equals(downStreamJobModelList, other.downStreamJobModelList) &&
                Objects.equals(rpTestItemName, other.rpTestItemName) &&
                Objects.equals(run, other.run);
    }
}
