package com.jurteg.jenkinsci.plugins.reportportal.plugin.view;

import com.jurteg.jenkinsci.plugins.reportportal.runtimeutils.JobNamingUtils;
import hudson.Extension;
import hudson.RelativePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public final class AdvancedNamingOptionsView extends AbstractDescribableImpl<AdvancedNamingOptionsView> {

    private String buildPattern;

    @DataBoundConstructor
    public AdvancedNamingOptionsView(String buildPattern) {
        this.buildPattern = buildPattern;
    }

    public String getBuildPattern() {
        return buildPattern;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AdvancedNamingOptionsView> {

        @Override
        public String getDisplayName() {
            return "";
        }

        public FormValidation doCheckBuildPattern(@QueryParameter String buildPattern, @RelativePath("..") @QueryParameter String jobToReportTitle) {
            if (buildPattern == null || jobToReportTitle == null) {
                return FormValidation.ok();
            } else {
                if (!buildPattern.isEmpty()) {
                    if (JobNamingUtils.getBuildNames(jobToReportTitle).isEmpty()) {
                        return FormValidation.warningWithMarkup(String.format("<i>NOTE: no build found for job '%s'.</i>", jobToReportTitle));
                    }
                    if (!JobNamingUtils.anyOfBuildsMatchPattern(jobToReportTitle, buildPattern)) {
                        return FormValidation.warningWithMarkup(String.format("<i>NOTE: none of the builds for job '%s' match this pattern.</i>", jobToReportTitle));
                    } else {
                        return FormValidation.okWithMarkup(String.format("<i>EXAMPLE: build name with applied pattern is: <b>'%s'</b></i>",
                                JobNamingUtils.getFirstMatchingBuildWithPatternApplied(jobToReportTitle, buildPattern)));
                    }
                }
            }
            return FormValidation.ok();
        }

    }
}
