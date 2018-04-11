package org.jenkinsci.plugins.reportportal;

import com.epam.reportportal.listeners.ListenerParameters;

import java.util.Set;

public class ReporterSettings {

    private boolean enableReporting;
    private String launchName;
    private String suiteName;
    private String jobName;
    private String testName;
    private String rpUuid;
    private String rpProject;
    private String rpEndpoint;
    private String launchDescription;
    private Set<String> tags;
    private ListenerParameters listenerParameters;

    public boolean isReportingEnabled() {
        return enableReporting;
    }

    public void setEnableReporting(boolean enableReporting) {
        this.enableReporting = enableReporting;
    }

    public ListenerParameters getListenerParameters() {
        return listenerParameters;
    }

    public String getLaunchName() {
        return launchName;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public String getJobName() {
        return jobName;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getRpUuid() {
        return rpUuid;
    }

    public String getRpProject() {
        return rpProject;
    }

    public String getRpEndpoint() {
        return rpEndpoint;
    }

    public String getTestName() {
        return testName;
    }

    public String getLaunchDescription() {
        return launchDescription;
    }

    public void setListenerParameters(ListenerParameters listenerParameters) {
        this.listenerParameters = listenerParameters;
    }

    public void setLaunchName(String launchName) {
        this.launchName = launchName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setRpUuid(String rpUuid) {
        this.rpUuid = rpUuid;
    }

    public void setRpProject(String rpProject) {
        this.rpProject = rpProject;
    }

    public void setRpEndpoint(String rpEndpoint) {
        this.rpEndpoint = rpEndpoint;
    }

    public void setLaunchDescription(String launchDescription) {
        this.launchDescription = launchDescription;
    }
}
