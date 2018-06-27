package org.jenkinsci.plugins.reportportal.plugin.view;

import java.util.List;

public interface JobView {

    String getRpTestItemName();

    String getDescription();

    AdvancedNamingOptionsView getAdvancedNamingOptions();

    boolean getEnableReporting();

    void setEnableReporting(boolean enableReporting);

    String getParentJobTitle();

    void setParentJobTitle(String parentJobTitle);

    String getJobToReportTitle();

    void setJobToReportTitle(String jobToReportTitle);

    String getTags();

    void setTags(String tags);

    List<DownStreamJobView> getDownStreamJobView();

    void setDownStreamJobView(List<DownStreamJobView> downStreamJobView);

    void setRpTestItemName(String rpTestItemName);

    void setDescription(String description);

}
