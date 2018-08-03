package com.jurteg.jenkinsci.plugins.reportportal.plugin;

import com.jurteg.jenkinsci.plugins.reportportal.plugin.view.GeneralView;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.XmlFile;
import hudson.model.AbstractDescribableImpl;
import hudson.model.RootAction;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Extension
public final class ReportPortalPlugin extends AbstractDescribableImpl<ReportPortalPlugin> implements ExtensionPoint, RootAction {

    private static Logger LOGGER = LoggerFactory.getLogger(ReportPortalPlugin.class);

    private final static String CONFIG_FILE = "com.jurteg.jenkinsci.plugins.reportportal.Configuration.xml";
    private final static String VERSION = "application.version";
    private GeneralView generalView;
    private String version;
    private final static String PROPERTY_FILE_PATH = "/properties.properties";

    public XmlFile getConfigFile() {
        return new XmlFile(new File(Jenkins.getInstance().getRootDir(), CONFIG_FILE));
    }

    public ReportPortalPlugin() throws IOException {
        XmlFile xml = getConfigFile();
        if (xml.exists()) {
            xml.unmarshal(this);
        }
        version = getVersion();
    }

    public HttpResponse doConfigSubmit(StaplerRequest req) throws ServletException, IOException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        generalView = null; // otherwise bindJSON will never clear it once set
        req.bindJSON(this, req.getSubmittedForm());
        getConfigFile().write(this);
        return FormApply.success(".");
    }

    public GeneralView getGeneralView() {
        return generalView;
    }

    public void setGeneralView(GeneralView generalView) {
        this.generalView = generalView;
    }

    public String getIconFileName() {
        return "gear.png";
    }

    public String getUrlName() {
        //Jenkins.getInstance().checkPermission(Jenkins.READ);
        return "report-portal-job-reporting";
    }

    /**
     * Default display name.
     */
    public String getDisplayName() {
        return "Report Portal Job Reporting";
    }

    public ReportPortalPluginDescriptor getDescriptor() {
        return (ReportPortalPluginDescriptor) super.getDescriptor();
    }

    public String getVersion() {
        Properties properties = new Properties();
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(PROPERTY_FILE_PATH);
            properties.load(resourceAsStream);
        } catch (Exception e) {
            LOGGER.error("Unable to load properties from: " + PROPERTY_FILE_PATH);
        }
        return properties.getProperty(VERSION);
    }


    @Extension
    public static class DescriptorImpl extends ReportPortalPluginDescriptor {
    }

}
