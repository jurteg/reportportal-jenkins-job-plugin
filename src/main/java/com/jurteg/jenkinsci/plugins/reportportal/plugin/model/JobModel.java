package com.jurteg.jenkinsci.plugins.reportportal.plugin.model;

import hudson.model.Run;
import hudson.model.TaskListener;
import io.reactivex.Maybe;

import java.util.List;

public interface JobModel extends ParentAware, ExecutableModel {

    Maybe<String> getRpTestItemId();

    void setRpTestItemId(Maybe<String> id);

    Run getRun();

    void setRun(Run run);

    Maybe<String> getJobLogItemId();

    void setJobLogItemId(Maybe<String> id);

    String getJobName();

    void resolveBaseName();

    void setNameAttributesList(List<String> nameAttributesList);

    String getRpTestItemName();

    String getDescription();

    String getTags();

    String getComposedName();

    List<DownStreamJobModel> getDownStreamJobModelList();

    void setDownStreamJobModelList(List<DownStreamJobModel> downStreamModelList);

    void addDownStreamJobModel(DownStreamJobModel jobModel);

    void start(Run run);

    LaunchModel getLaunch();

    String getTestItemType();

    boolean isClone();

    void setIsClone(boolean isClone);

    void setTaskListener(TaskListener listener);

}
