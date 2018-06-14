package org.jenkinsci.plugins.reportportal.plugin.view.job;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import org.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;

public class UpStreamJobView extends AbstractDescribableImpl<UpStreamJobView> implements JobView {

    private String jobToReportTitle;
    private String rpTestItemName;
    private String tags;
    private String description;
    private boolean enableReporting;
    private List<DownStreamJobView> downStreamJobView;

    @DataBoundConstructor
    public UpStreamJobView(List<DownStreamJobView> downStreamJobView, String rpTestItemName, String jobToReportTitle, String description, boolean enableReporting, String tags) {
        this.rpTestItemName = rpTestItemName;
        this.jobToReportTitle = jobToReportTitle;
        this.description = description;
        this.enableReporting = enableReporting;
        this.tags = tags;
        this.downStreamJobView = downStreamJobView;
    }

    public List<DownStreamJobModel> createDownStreamJobModelList(JobModel parent) {
        List<DownStreamJobModel> modelList = new ArrayList<>();
        if(downStreamJobView != null) {
            for (DownStreamJobView view : downStreamJobView) {
                modelList.add(view.createModel(parent));
            }
        }
        return modelList;
    }

    @Override
    public String getParentJobTitle() {
        throw new UnsupportedOperationException("Upstream job view could not have parent views.");
    }

    @Override
    public void setParentJobTitle(String parentJobTitle) {
        throw new UnsupportedOperationException("Upstream job view could not have parent views.");
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getRpTestItemName() {
        return rpTestItemName;
    }

    public void setRpTestItemName(String rpTestItemName) {
        this.rpTestItemName = rpTestItemName;
    }

    public String getJobToReportTitle() {
        return jobToReportTitle;
    }

    public void setJobToReportTitle(String jobToReportTitle) {
        this.jobToReportTitle = jobToReportTitle;
    }

    public List<DownStreamJobView> getDownStreamJobView() {
        return downStreamJobView;
    }

    public List<DownStreamJobView> getDownStreamJobView3() {
        return downStreamJobView;
    }

    public void setDownStreamJobView(List<DownStreamJobView> downStreamJobView) {
        this.downStreamJobView = downStreamJobView;
    }

    public boolean getEnableReporting() {
        return enableReporting;
    }

    public void setEnableReporting(boolean enableReporting) {
        this.enableReporting = enableReporting;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<UpStreamJobView> {

        @Override
        public String getDisplayName() {
            return ">>>>>>>>";
        }

        /*
        public ListBoxModel doFillJobToReportTitleItems() {
            ListBoxModel jenkinsJobList = new ListBoxModel(getJobOptions());
            return jenkinsJobList;
        }

        private List<ListBoxModel.Option> getJobOptions() {
            List<ListBoxModel.Option> jobNameList = new ArrayList<>();
            for (String job : JenkinsJobUtils.getJenkinsJobNamesList()) {
                jobNameList.add(new ListBoxModel.Option(job.trim()));
            }
            return jobNameList;
        }
        */

    }
}
