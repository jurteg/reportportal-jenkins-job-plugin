package com.jurteg.jenkinsci.plugins.reportportal.plugin.view;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneralView extends AbstractDescribableImpl<GeneralView> {

    private final ConfigView config;
    private final List<LaunchView> entries;

    public List<LaunchView> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public ConfigView getConfig() {
        return config;
    }

    public boolean hasEntries() {
        return !getEntries().isEmpty();
    }

    @DataBoundConstructor
    public GeneralView(ConfigView config, List<LaunchView> entries) {
        this.config = config;
        this.entries = entries != null ? new ArrayList<>(entries) : Collections.<LaunchView>emptyList();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<GeneralView> {

        public List<Descriptor> getEntriesDescriptors() {
            Jenkins jenkins = Jenkins.getInstance();
            return ImmutableList.of(jenkins.getDescriptor(LaunchView.class));
        }

    }

}
