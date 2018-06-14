package org.jenkinsci.plugins.reportportal.plugin.model;

import hudson.model.Run;
import io.reactivex.Maybe;

import java.util.List;

public interface JobModel extends ParentAware {

    Maybe<String> getRpTestItemId();

    void setRpTestItemId(Maybe<String> id);

    Run getRun();

    void setRun(Run run);

    Maybe<String> getJobLogItemId();

    void setJobLogItemId(Maybe<String> id);

    String getJobName();

    String getRpTestItemName();

    String getDescription();

    String getTags();

    List<DownStreamJobModel> getDownStreamJobModelList();

    void setDownStreamJobModelList(List<DownStreamJobModel> downStreamModelList);

    void addDownStreamJobModel(DownStreamJobModel jobModel);

    void start();

    void start(Run run);

    void finish(Run run);

    LaunchModel getLaunch();

    String getTestItemType();



}
