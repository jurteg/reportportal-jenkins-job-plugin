package org.jenkinsci.plugins.reportportal.plugin.view;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;

public final class Config extends AbstractDescribableImpl<Config> {
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
    public Config(String endpoint, String uuid, String project) {
        this.endpoint = endpoint;
        this.uuid = uuid;
        this.project = project;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Config> {

        @Override
        public String getDisplayName() {
            return "Report Portal Configuration";
        }

        public FormValidation doCheckUuid() {
            return FormValidation.ok();
        }

        public FormValidation doCheckEndpoint() {
            return FormValidation.ok();
        }

        public FormValidation doCheckProject() {
            return FormValidation.ok();
        }

    }

}
