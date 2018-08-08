package com.jurteg.jenkinsci.plugins.reportportal.plugin.view;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public final class ConfigView extends AbstractDescribableImpl<ConfigView> {
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

    public boolean isSet() {
        return uuid != null && project != null && endpoint != null;
    }

    @DataBoundConstructor
    public ConfigView(String endpoint, String uuid, String project) {
        this.endpoint = endpoint;
        this.uuid = uuid;
        this.project = project;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ConfigView> {

        @Override
        public String getDisplayName() {
            return "Report Portal Configuration";
        }


        public FormValidation doCheckUuid(@QueryParameter String uuid) {
            if(uuid.isEmpty()) {
                return FormValidation.error("UUID must not be blank");
            }
            return FormValidation.ok();
        }
        public FormValidation doCheckEndpoint(@QueryParameter String endpoint) {
            if(endpoint.isEmpty()) {
                return FormValidation.error("Server address must not be blank");
            }
            if(!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                return FormValidation.error("Server address must be starting from http:// or https://");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckProject(@QueryParameter String project) {
            if(project.isEmpty()) {
                return FormValidation.error("Project name must not be blank");
            }
            return FormValidation.ok();
        }

    }

}
