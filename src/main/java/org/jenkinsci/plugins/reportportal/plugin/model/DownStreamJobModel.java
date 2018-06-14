package org.jenkinsci.plugins.reportportal.plugin.model;

import org.jenkinsci.plugins.reportportal.plugin.utils.LaunchUtils;
import org.jenkinsci.plugins.reportportal.plugin.view.job.JobView;

import java.util.Objects;

public class DownStreamJobModel extends AbstractJobModel {

    public DownStreamJobModel(String rpTestItemName, DownStreamJobModel parent) {
        this.rpTestItemName = rpTestItemName;
        this.parent = parent;
    }

    public DownStreamJobModel(JobView view, ParentAware parent) {
        this.jobName = view.getJobToReportTitle();
        this.description = view.getDescription();
        this.tags = view.getTags();
        this.rpTestItemName = view.getRpTestItemName();
        this.parent = parent;
    }

    public DownStreamJobModel(JobModel jobModel) {
        this.jobName = jobModel.getJobName();
        this.description = jobModel.getDescription();
        this.tags = jobModel.getTags();
        this.rpTestItemName = jobModel.getRpTestItemName();
        this.parent = (JobModel) jobModel.getParent();
        this.run = jobModel.getRun();
        this.downStreamJobModelList = jobModel.getDownStreamJobModelList();
    }

    @Override
    public void start() {
        if (rpTestItemId != null) {
            throw new IllegalStateException("Attempting to start already running RP item: " + rpTestItemId.blockingGet());
        }
        if (((JobModel) this.parent).getRpTestItemId() == null) {
            throw new IllegalStateException(String.format("Unable to run RP item for job model '%s' because parent item '%s' is not running.", toString(), parent.toString()));
        }
        rpTestItemId = LaunchUtils.startTestItem(getLaunch().getRp(), ((JobModel) this.parent).getRpTestItemId(), rpTestItemName, description, processTags(tags), getTestItemType());
        startLogItem();
    }

    public String getTestItemType() {
        return "SUITE";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        DownStreamJobModel other = (DownStreamJobModel) obj;
        return Objects.equals(jobName, other.jobName) &&
                Objects.equals(description, other.description) &&
                Objects.equals(tags, other.tags) &&
                Objects.equals(downStreamJobModelList, other.downStreamJobModelList) &&
                Objects.equals(rpTestItemName, other.rpTestItemName) &&
                Objects.equals(run, other.run);
    }

}
