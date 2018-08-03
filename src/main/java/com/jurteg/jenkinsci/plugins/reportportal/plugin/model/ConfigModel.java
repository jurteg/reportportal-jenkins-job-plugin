package com.jurteg.jenkinsci.plugins.reportportal.plugin.model;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.ConfigView;

public class ConfigModel {

    private String uuid;
    private String project;
    private String endpoint;

    public ConfigModel(ConfigView config) {
        this.uuid = config.getUuid();
        this.project = config.getProject();
        this.endpoint = config.getEndpoint();
    }

    public String getUuid() {
        return uuid;
    }

    public String getProject() {
        return project;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
