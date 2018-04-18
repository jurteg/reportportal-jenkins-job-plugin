package org.jenkinsci.plugins.reportportal.plugin;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.XmlFile;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.RootAction;
import hudson.util.FormApply;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Extension
public final class ReportPortalPlugin extends AbstractDescribableImpl<ReportPortalPlugin> implements ExtensionPoint, RootAction {

    private ReportPortalGeneralView generalView;

    public XmlFile getConfigFile() {
        return new XmlFile(new File(Jenkins.getInstance().getRootDir(), "stuff.xml"));
    }

    public ReportPortalPlugin() throws IOException {
        XmlFile xml = getConfigFile();
        if (xml.exists()) {
            xml.unmarshal(this);
        }
    }

    public HttpResponse doConfigSubmit(StaplerRequest req) throws ServletException, IOException {
        generalView = null; // otherwise bindJSON will never clear it once set
        req.bindJSON(this, req.getSubmittedForm());
        getConfigFile().write(this);
        return FormApply.success(".");
    }

    public ReportPortalGeneralView getGeneralView() {
        return generalView;
    }

    public void setGeneralView(ReportPortalGeneralView generalView) {
        this.generalView = generalView;
    }

    public String getIconFileName() {
        return "gear.png";
    }

    public String getUrlName() {
        return "report-portal-job-reporting";
    }

    /**
     * Default display name.
     */
    public String getDisplayName() {
        return "Report Portal Job Reporting";
    }

    /*
    public ReportPortalPluginDescriptor getDescriptor() {
        return (ReportPortalPluginDescriptor) super.getDescriptor();
    }
    */

    @Extension
    public static class DescriptorImpl extends ReportPortalPluginDescriptor {
    }

    public static final class ReportPortalConfig extends AbstractDescribableImpl<ReportPortalConfig> {
        private String uuid;
        private String project;
        private String endpoint;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        @DataBoundConstructor
        public ReportPortalConfig(String endpoint, String uuid, String project) {
            this.endpoint = endpoint;
            this.uuid = uuid;
            this.project = project;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<ReportPortalConfig> {

            @Override
            public String getDisplayName() {
                return "Report Portal Configuration";
            }

            public void doCheckUuid() {

            }

            public void doCheckEndpoint() {

            }

            public void doCheckProject() {

            }

        }

    }

    public static final class ReportPortalGeneralView extends AbstractDescribableImpl<ReportPortalGeneralView> {

        private final ReportPortalConfig config;
        private final List<ConfigurableJenkinsJob> entries;

        public List<ConfigurableJenkinsJob> getEntries() {
            return Collections.unmodifiableList(entries);
        }

        public ReportPortalConfig getConfig() {
            return config;
        }

        @DataBoundConstructor
        public ReportPortalGeneralView(ReportPortalConfig config, List<ConfigurableJenkinsJob> entries) {
            this.config = config;
            this.entries = entries != null ? new ArrayList<>(entries) : Collections.<ConfigurableJenkinsJob>emptyList();
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<ReportPortalGeneralView> {
        }

    }


    public static final class ConfigurableJenkinsJob extends AbstractDescribableImpl<ConfigurableJenkinsJob> {

        private Entry jobName;
        private boolean enableReporting;
        private String launchName;
        private String suiteName;
        private String testName;
        private String tags;
        private String launchDescription;

        public boolean isSubJobAllowed() {
            return subJobAllowed;
        }

        public void setSubJobAllowed(boolean subJobAllowed) {
            this.subJobAllowed = subJobAllowed;
        }

        private boolean subJobAllowed = false;

        private final ReportPortalConfig config;

        @DataBoundConstructor
        public ConfigurableJenkinsJob(boolean subJobAllowed, Entry jobName, ReportPortalConfig config, boolean enableReporting, String launchName, String suiteName,String testName, String tags, String launchDescription) {
            this.config = config;
            this.jobName = jobName;
            this.enableReporting = enableReporting;
            this.launchName = launchName;
            this.suiteName = suiteName;
            this.testName = testName;
            this.tags = tags;
            this.launchDescription = launchDescription;
            this.subJobAllowed = subJobAllowed;
        }

        public boolean getEnableReporting() {
            return enableReporting;
        }

        public void setEnableReporting(boolean enableReporting) {
            this.enableReporting = enableReporting;
        }

        public Entry getJobName() {
            return jobName;
        }

        public String getLaunchName() {
            return launchName;
        }

        public void setLaunchName(String launchName) {
            this.launchName = launchName;
        }

        public String getSuiteName() {
            return suiteName;
        }

        public void setSuiteName(String suiteName) {
            this.suiteName = suiteName;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public String getLaunchDescription() {
            return launchDescription;
        }

        public void setLaunchDescription(String launchDescription) {
            this.launchDescription = launchDescription;
        }

        public ReportPortalConfig getConfig() {
            return config;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<ConfigurableJenkinsJob> {
            @Override
            public String getDisplayName() {
                return "Jenkins Job Options";
            }

            public List<Descriptor> getEntryDescriptors() {
                Jenkins jenkins = Jenkins.getInstance();
                return ImmutableList.of(jenkins.getDescriptor(TextEntry.class), jenkins.getDescriptor(SelectableExistingJobEntry.class));
            }
        }

    }

    public static abstract class Entry extends AbstractDescribableImpl<Entry> {
    }

    public static final class SelectableExistingJobEntry extends Entry {

        private final String choice;

        @DataBoundConstructor
        public SelectableExistingJobEntry(String choice) {
            this.choice = choice;
        }

        public String getChoice() {
            return choice;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<Entry> {

            @Override
            public String getDisplayName() {
                return "Select existing job";
            }

            private List<ListBoxModel.Option> getJobOptions() {
                List<ListBoxModel.Option> jobNameList = new ArrayList<>();
                for(String job :  Jenkins.getInstance().getJobNames()) {
                    jobNameList.add(new ListBoxModel.Option(job.trim()));
                }
                return jobNameList;
            }


            public ListBoxModel doFillChoiceItems() {
                ListBoxModel listBoxModel = new ListBoxModel();
                listBoxModel.addAll(getJobOptions());
                return listBoxModel;
            }


        }
    }

    public static final class TextEntry extends Entry {

        private final String text;

        @DataBoundConstructor
        public TextEntry(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<Entry> {
            @Override
            public String getDisplayName() {
                return "Specify job name manually";
            }
        }
    }


}
