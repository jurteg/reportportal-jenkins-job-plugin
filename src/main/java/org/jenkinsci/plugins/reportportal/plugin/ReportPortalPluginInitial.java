package org.jenkinsci.plugins.reportportal.plugin;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import hudson.Extension;
import hudson.Plugin;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.reportportal.ReporterSettings;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class ReportPortalPlugin extends Plugin {

    private final static java.util.logging.Logger LOGGER = Logger.getLogger(ReportPortalPlugin.class.getName());
    private static final String SPACE = " ";

    private boolean enableReporting;
    private String launchName;
    private String suiteName;
    private String jobName;
    private String testName;
    private String tags;
    private String launchDescription;

    private String rpUuid;
    private String rpProject;
    private String rpEndpoint;

    @Override
    public void configure(StaplerRequest req, JSONObject formData) {
        enableReporting = formData.optBoolean("enableReporting", false);
        launchName = formData.optString("launchName", "");
        suiteName = formData.optString("suiteName", "");
        jobName = formData.optString("jobName", "");
        testName = formData.optString("testName", "");
        tags = formData.optString("tags", "");
        testName = formData.optString("testName", "");
        launchDescription = formData.optString("launchDescription", "");

        rpUuid = formData.optString("rpUuid", "");
        rpProject = formData.optString("rpProject", "");
        rpEndpoint = formData.optString("rpEndpoint", "");

        try {
            save();
        } catch (IOException e)
        {
            LOGGER.log(Level.WARNING, "Error saving Report Portal settings: " + e.getMessage());
        }
    }

    @Override
    public void start()
    {
        try {
            load();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading Report Portal Jenkins Job plugin: " + e.getMessage());
        }
    }

    public ReporterSettings getSettings() {
        ReporterSettings settings = new ReporterSettings();
        ListenerParameters listenerParameters = new ListenerParameters(PropertiesLoader.load());
        listenerParameters.setUuid(rpUuid);
        listenerParameters.setProjectName(rpProject);
        listenerParameters.setBaseUrl(rpEndpoint);
        listenerParameters.setEnable(true);
        settings.setListenerParameters(listenerParameters);
        settings.setEnableReporting(enableReporting);
        settings.setJobName(jobName);
        settings.setTags(processTags(tags));
        settings.setLaunchDescription(launchDescription);

        if(launchName == null || launchName.isEmpty()) {
            settings.setLaunchName(StringUtils.capitalize(jobName.replace("_", SPACE)));
        } else {
            settings.setLaunchName(launchName);
        }

        if(suiteName == null || suiteName.isEmpty()) {
            settings.setSuiteName(null);
        } else {
            settings.setSuiteName(suiteName);
        }

        if(testName == null || testName.isEmpty()) {
            settings.setTestName("Tests Item");
        } else {
            settings.setTestName(testName);
        }

        LOGGER.log(Level.WARNING, "Reporter settings: " + settings.toString());

        return settings;
    }

    public boolean isEnableReporting() {
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

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
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

    public String getRpUuid() {
        return rpUuid;
    }

    public void setRpUuid(String rpUuid) {
        this.rpUuid = rpUuid;
    }

    public String getRpProject() {
        return rpProject;
    }

    public void setRpProject(String rpProject) {
        this.rpProject = rpProject;
    }

    public String getRpEndpoint() {
        return rpEndpoint;
    }

    public void setRpEndpoint(String rpEndpoint) {
        this.rpEndpoint = rpEndpoint;
    }

    private Set<String> processTags(String delimitedString) {
        Set<String> tags = new HashSet<>();
        if(delimitedString != null) {
            for (String tag : delimitedString.split(SPACE)) {
                tags.add(tag);
            }
        }
        return tags;
    }



}
