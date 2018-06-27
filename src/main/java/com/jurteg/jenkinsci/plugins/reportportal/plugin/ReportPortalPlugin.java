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

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

@Extension
public final class ReportPortalPlugin extends AbstractDescribableImpl<ReportPortalPlugin> implements ExtensionPoint, RootAction {

    private final static String CONFIG_FILE = "rpconfig.xml";
    private GeneralView generalView;

    public XmlFile getConfigFile() {
        return new XmlFile(new File(Jenkins.getInstance().getRootDir(), CONFIG_FILE));
    }

    public ReportPortalPlugin() throws IOException {
        XmlFile xml = getConfigFile();
        if (xml.exists()) {
            xml.unmarshal(this);
        }
    }

    public HttpResponse doConfigSubmit(StaplerRequest req) throws ServletException, IOException {
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

    @Extension
    public static class DescriptorImpl extends ReportPortalPluginDescriptor {
    }

}
