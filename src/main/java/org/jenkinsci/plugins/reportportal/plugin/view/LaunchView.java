package org.jenkinsci.plugins.reportportal.plugin.view;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.jenkinsci.plugins.reportportal.plugin.view.job.UpStreamJobView;
import org.kohsuke.stapler.DataBoundConstructor;

public class LaunchView extends AbstractDescribableImpl<LaunchView> {

    private String launchName;
    private String tags;
    private String launchDescription;
    private boolean enableReporting;
    private final Config config;
    private UpStreamJobView upStreamJobView;

    @DataBoundConstructor
    public LaunchView(UpStreamJobView upStreamJobView, Config config, String launchName, String tags, String launchDescription, boolean enableReporting) {
        this.config = config;
        this.launchName = launchName;
        this.tags = tags;
        this.launchDescription = launchDescription;
        this.enableReporting = enableReporting;
        this.upStreamJobView = upStreamJobView;
    }

    public boolean getEnableReporting() {
        return enableReporting;
    }

    public void setEnableReporting(boolean enableReporting) {
        this.enableReporting = enableReporting;
    }

    public String getLaunchName() {
        return launchName;
    }

    public void setLaunchName(String launchName) {
        this.launchName = launchName;
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

    public Config getConfig() {
        return config;
    }

    public UpStreamJobView getUpStreamJobView() {
        return upStreamJobView;
    }

    public void setUpStreamJobView(UpStreamJobView upStreamJobView) {
        this.upStreamJobView = upStreamJobView;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<LaunchView> {

        @Override
        public String getDisplayName() {
            return ">>>>>>>>";
        }

    }

}
