package org.jenkinsci.plugins.reportportal.plugin.view.job;

import org.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import org.jenkinsci.plugins.reportportal.plugin.model.JobModel;

import java.util.ArrayList;
import java.util.List;

public interface JobView {

    String getRpTestItemName();

    String getDescription();

    boolean getEnableReporting();

    void setEnableReporting(boolean enableReporting);

    String getParentJobTitle();

    void setParentJobTitle(String parentJobTitle);

    String getJobToReportTitle();

    void setJobToReportTitle(String jobToReportTitle);

    String getTags();

    void setTags(String tags);

    List<DownStreamJobView> getDownStreamJobView();

    <T extends JobView> List<T> getDownStreamJobView3();

    void setDownStreamJobView(List<DownStreamJobView> downStreamJobView);

    void setRpTestItemName(String rpTestItemName);

    void setDescription(String description);

    /*

    default JobModel createModel2(JobModel parent) throws IllegalAccessException, InstantiationException{
        DownStreamJobModel model = new DownStreamJobModel(getRpTestItemName(), parent);
        model.fillFields(this);
        if (getDownStreamJobView3() != null && !getDownStreamJobView3().isEmpty()) {
            List<JobModel> childModelList = new ArrayList<>();
            for (JobView view : getDownStreamJobView3()) {
                JobModel child = view.createModel2(model);
                childModelList.add(child);
            }
            model.setModelList(childModelList);
        }
        return model;
    }


    default DownStreamJobModel createModel(DownStreamJobModel parent){
        DownStreamJobModel model = new DownStreamJobModel(this, parent);
        model.fillFields(this);
        if (getDownStreamJobView() != null && !getDownStreamJobView().isEmpty()) {
            List<JobModel> childModelList = new ArrayList<>();
            for (DownStreamJobView view : getDownStreamJobView()) {
                DownStreamJobModel child = view.createModel(model);
                childModelList.add(child);
            }
            model.setModelList(childModelList);
        }
        return model;
    }

    */

}
