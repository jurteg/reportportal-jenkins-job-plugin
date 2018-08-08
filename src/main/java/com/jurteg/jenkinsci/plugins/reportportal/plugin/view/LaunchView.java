package com.jurteg.jenkinsci.plugins.reportportal.plugin.view;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class LaunchView extends AbstractDescribableImpl<LaunchView> {

    private String launchName;
    private String tags;
    private String launchDescription;
    private boolean enableReporting;
    private final ConfigView config;
    private UpStreamJobView upStreamJobView;

    @DataBoundConstructor
    public LaunchView(UpStreamJobView upStreamJobView, ConfigView config, String launchName, String tags, String launchDescription, boolean enableReporting) {
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

    public ConfigView getConfig() {
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

        public FormValidation doCheckLaunchName(@QueryParameter String launchName) {
            if (launchName.isEmpty()) {
                return FormValidation.warning("WARNING: Launch name is blank and will be filled with corresponding Multijob(Upstream) name.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckLaunchDescription(@QueryParameter String launchDescription) {
            return FormValidation.okWithMarkup("<i>NOTE: the description section on RP supports markdown, so you could format your text.</i>");
        }

        public FormValidation doCheckTags(@QueryParameter String tags) {
            return FormValidation.okWithMarkup("<i>NOTE: use semicolon for separating tags.</i>");
        }


    }

}
