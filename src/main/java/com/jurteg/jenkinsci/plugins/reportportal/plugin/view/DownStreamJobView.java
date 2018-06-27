package org.jenkinsci.plugins.reportportal.plugin.view;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import org.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import org.jenkinsci.plugins.reportportal.plugin.utils.JenkinsJobUtils;
import org.jenkinsci.plugins.reportportal.plugin.utils.ViewUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.ArrayList;
import java.util.List;

public class DownStreamJobView extends AbstractDescribableImpl<DownStreamJobView> implements JobView {

    private boolean enableReporting;
    private String buildPattern;
    private String tags;
    private String description;
    private String parentJobTitle;
    private String jobToReportTitle;
    private String rpTestItemName;
    private List<DownStreamJobView> downStreamJobView;
    private AdvancedNamingOptionsView advancedNamingOptions;

    @DataBoundConstructor
    public DownStreamJobView(List<DownStreamJobView> downStreamJobView, String parentJobTitle, String jobToReportTitle, AdvancedNamingOptionsView advancedNamingOptions, String rpTestItemName, String tags, String description, boolean enableReporting) {
        this.enableReporting = enableReporting;
        this.tags = tags;
        this.description = description;
        this.parentJobTitle = parentJobTitle;
        this.jobToReportTitle = jobToReportTitle;
        this.advancedNamingOptions = advancedNamingOptions;
        this.rpTestItemName = rpTestItemName;
        this.downStreamJobView = downStreamJobView;
    }

    public AdvancedNamingOptionsView getAdvancedNamingOptions() {
        return advancedNamingOptions;
    }

    public List<DownStreamJobView> getDownStreamJobView() {
        return downStreamJobView;
    }

    public void setDownStreamJobView(List<DownStreamJobView> downStreamJobView) {
        this.downStreamJobView = downStreamJobView;
    }

    public String getRpTestItemName() {
        return rpTestItemName;
    }

    public void setRpTestItemName(String rpTestItemName) {
        this.rpTestItemName = rpTestItemName;
    }

    public String getParentJobTitle() {
        return parentJobTitle;
    }

    public String getJobToReportTitle() {
        return jobToReportTitle;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setJobToReportTitle(String jobToReportTitle) {
        this.jobToReportTitle = jobToReportTitle;
    }

    public void setParentJobTitle(String parentJobTitle) {
        this.parentJobTitle = parentJobTitle;
    }

    DownStreamJobModel createModel(JobModel parent){
        DownStreamJobModel model = new DownStreamJobModel(this, parent);
        if (getDownStreamJobView() != null && !getDownStreamJobView().isEmpty()) {
            List<DownStreamJobModel> childModelList = new ArrayList<>();
            for (DownStreamJobView view : getDownStreamJobView()) {
                DownStreamJobModel child = view.createModel(model);
                childModelList.add(child);
            }
            model.setDownStreamJobModelList(childModelList);
        }
        return model;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DownStreamJobView> {

        @Override
        public String getDisplayName() {
            return ">>>>>>>>";
        }

        public FormValidation doCheckJobToReportTitle(@QueryParameter String jobToReportTitle) {
            if (jobToReportTitle.isEmpty()) {
                return FormValidation.error("Jenkins job name must not be blank!");
            }
            if (Jenkins.getInstance().getItem(jobToReportTitle) == null) {
                return FormValidation.warning(String.format("WARNING: Job with name '%s' wasn't found ob Jenkins. Typo?", jobToReportTitle));
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckParentJobTitle(@QueryParameter String parentJobTitle) {
            if (parentJobTitle.isEmpty()) {
                return FormValidation.error("Parent jenkins job name must not be blank!");
            }
            if (Jenkins.getInstance().getItem(parentJobTitle) == null) {
                return FormValidation.warning(String.format("WARNING: Job with name '%s' wasn't found ob Jenkins. Typo?", parentJobTitle));
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRpTestItemName(@QueryParameter String rpTestItemName) {
            if (rpTestItemName.isEmpty()) {
                return FormValidation.warning("WARNING: 'Downstream display name' is blank and will be filled with Downstream job original name.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckDescription(@QueryParameter String launchDescription) {
            return FormValidation.okWithMarkup("<i>NOTE: the description section on RP supports markdown, so you could format your text.</i>");
        }

        public FormValidation doCheckTags(@QueryParameter String tags) {
            return FormValidation.okWithMarkup("<i>NOTE: use semicolon for separating tags.</i>");
        }

        private List<ListBoxModel.Option> getParentJobOptions() {
            return getParentJobOptions(ViewUtils.getExistingUpStreamJobNames());
        }

        private List<ListBoxModel.Option> getParentJobOptions(List<String> jobNames) {
            List<ListBoxModel.Option> jobNameList = new ArrayList<>();
            for (String job : jobNames) {
                jobNameList.add(new ListBoxModel.Option(job.trim()));
            }
            if (jobNames.isEmpty()) {
                jobNameList.add(new ListBoxModel.Option("No upstream jobs found."));
            }
            return jobNameList;
        }

        private List<ListBoxModel.Option> getChildJobOptions(String parentJobTitle) {
            List<ListBoxModel.Option> jobNameList = new ArrayList<>();
            if (parentJobTitle == null) {
                jobNameList.add(new ListBoxModel.Option("No upstream jobs are set."));
                return jobNameList;
            }
            for (String job : ViewUtils.getJenkinsJobChildrenNames(parentJobTitle)) {
                jobNameList.add(new ListBoxModel.Option(job.trim()));
            }
            if(jobNameList.isEmpty()) {
                jobNameList.add(new ListBoxModel.Option("No children found for " + parentJobTitle));
            }
            return jobNameList;
        }

        private List<ListBoxModel.Option> getJobOptions() {
            List<ListBoxModel.Option> jobNameList = new ArrayList<>();
            for (String job : JenkinsJobUtils.getJenkinsJobNamesList()) {
                jobNameList.add(new ListBoxModel.Option(job.trim()));
            }
            return jobNameList;
        }

        /**
         * This method determines the values of the album drop-down list box.
         */
        /*
        public ListBoxModel doFillParentJobTitleItems() {
            ListBoxModel m = new ListBoxModel(getParentJobOptions());
            return m;
        }

        public ListBoxModel doFillJobToReportTitleItems(@QueryParameter String parentJobTitle) {
            ListBoxModel m = new ListBoxModel(getJobOptions() );
            return m;
            //return new ListBoxModel(getChildJobOptions(parentJobTitle));
        }
        */

    }
}
