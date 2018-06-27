package com.jurteg.jenkinsci.plugins.reportportal.plugin.view;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.ArrayList;
import java.util.List;

public class UpStreamJobView extends AbstractDescribableImpl<UpStreamJobView> implements JobView {

    private String jobToReportTitle;
    private String rpTestItemName;
    private String tags;
    private String description;
    private boolean enableReporting;
    private List<DownStreamJobView> downStreamJobView;
    private AdvancedNamingOptionsView advancedNamingOptions;

    @DataBoundConstructor
    public UpStreamJobView(List<DownStreamJobView> downStreamJobView, String rpTestItemName, String jobToReportTitle, AdvancedNamingOptionsView advancedNamingOptions, String description, boolean enableReporting, String tags) {
        this.rpTestItemName = rpTestItemName;
        this.jobToReportTitle = jobToReportTitle;
        this.advancedNamingOptions = advancedNamingOptions;
        this.description = description;
        this.enableReporting = enableReporting;
        this.tags = tags;
        this.downStreamJobView = downStreamJobView;
    }

    public AdvancedNamingOptionsView getAdvancedNamingOptions() {
        return advancedNamingOptions;
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

    public List<DownStreamJobModel> createDownStreamJobModelList(JobModel parent) {
        List<DownStreamJobModel> modelList = new ArrayList<>();
        if (downStreamJobView != null && !downStreamJobView.isEmpty()) {
            for (DownStreamJobView view : downStreamJobView) {
                modelList.add(view.createModel(parent));
            }
        }
        return modelList;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<UpStreamJobView> {

        @Override
        public String getDisplayName() {
            return ">>>>>>>>";
        }

        public FormValidation doCheckJobToReportTitle(@QueryParameter String jobToReportTitle) {
            if(jobToReportTitle != null) {
                if (jobToReportTitle.isEmpty()) {
                    return FormValidation.error("Jenkins job name must not be blank!");
                }
                if (Jenkins.getInstance().getItem(jobToReportTitle) == null) {
                    return FormValidation.warning(String.format("WARNING: Job with name '%s' wasn't found ob Jenkins. Typo?", jobToReportTitle));
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRpTestItemName(@QueryParameter String rpTestItemName) {
            if (rpTestItemName != null && rpTestItemName.isEmpty()) {
                return FormValidation.warning("WARNING: 'Multijob display name' is blank and will be filled with Multijob original name.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckDescription(@QueryParameter String launchDescription) {
            return FormValidation.okWithMarkup("<i>NOTE: the description section on RP supports markdown, so you could format your text.</i>");
        }

        public FormValidation doCheckTags(@QueryParameter String tags) {
            return FormValidation.okWithMarkup("<i>NOTE: use semicolon for separating tags.</i>");
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
