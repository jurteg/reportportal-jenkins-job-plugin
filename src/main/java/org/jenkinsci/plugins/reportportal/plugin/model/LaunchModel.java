package org.jenkinsci.plugins.reportportal.plugin.model;

import com.epam.reportportal.service.Launch;
import com.google.common.base.Supplier;
import hudson.model.Run;
import org.jenkinsci.plugins.reportportal.plugin.utils.LaunchUtils;
import org.jenkinsci.plugins.reportportal.plugin.view.Config;
import org.jenkinsci.plugins.reportportal.plugin.view.LaunchView;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LaunchModel implements ParentAware {

    private static final String SPACE = " ";

    private boolean reportingEnabled;
    private String name;
    private String description;
    private String tags;
    private UpstreamJobModel upstreamJobModel;
    private Run run;
    private ParentAware parent;
    private Launch rp;

    public LaunchModel(LaunchView launchView, Run run) {
        this.name = launchView.getLaunchName();
        this.description = launchView.getLaunchDescription();
        this.tags = launchView.getTags();
        this.upstreamJobModel = new UpstreamJobModel(launchView.getUpStreamJobView(), this, run);
        this.run = run;
        this.reportingEnabled = launchView.getEnableReporting();
    }

    public ParentAware getParent() {
        //throw new UnsupportedOperationException("Launch Model can't have any parent.");
        return parent;
    }

    public void start(Run run, Config config) {
        if (rp != null) {
            throw new IllegalStateException("Attempting to start already running Launch Model: " + toString());
        }
        this.run = run;
        rp = LaunchUtils.startLaunch(config, name, description, processTags(tags));
        upstreamJobModel.start();
    }

    public void finish() {
        LaunchUtils.finishLaunch(rp);
    }

    public Launch getRp() {
        return rp;
    }

    public void setParent(ParentAware parent) {
        this.parent = parent;
    }

    public boolean isReportingEnabled() {
        return reportingEnabled;
    }

    public void setReportingEnabled(boolean reportingEnabled) {
        this.reportingEnabled = reportingEnabled;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public UpstreamJobModel getUpstreamJobModel() {
        return upstreamJobModel;
    }

    public void setUpstreamJobModel(UpstreamJobModel upstreamJobModel) {
        this.upstreamJobModel = upstreamJobModel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, tags, upstreamJobModel, run);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        LaunchModel other = (LaunchModel) obj;
        return Objects.equals(name, other.name) &&
                Objects.equals(description, other.description) &&
                Objects.equals(tags, other.tags) &&
                Objects.equals(upstreamJobModel, other.upstreamJobModel) &&
                Objects.equals(run, other.run);
    }

    protected Set<String> processTags(String delimitedString) {
        Set<String> tags = new HashSet<>();
        if (delimitedString != null) {
            for (String tag : delimitedString.split(SPACE)) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
