package org.jenkinsci.plugins.reportportal.plugin.model;

import com.epam.reportportal.service.Launch;
import hudson.model.Run;
import io.reactivex.Maybe;
import org.jenkinsci.plugins.reportportal.plugin.utils.LaunchUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractJobModel implements JobModel, Cloneable {

    private static final String SEMICOLON = ";";

    protected String jobName;
    protected String buildNamePattern;
    protected String description;
    protected String tags;
    protected List<DownStreamJobModel> downStreamJobModelList;
    protected String rpTestItemName;
    protected ParentAware parent;
    protected Maybe<String> rpTestItemId;
    protected Run run;
    protected Maybe<String> jobLogItemId;

    @Override
    public void start(Run run) {
        if (this.run != null) {
            if (this.run.getFullDisplayName().equals(run.getFullDisplayName()) || this.run == run) {
                throw new IllegalStateException("Attempting to set the same Run to JobModel:" + toString());
            }
            throw new IllegalStateException("Attempting to override already existing run in JobModel: " + toString());
        }
        this.run = run;
        start();
    }

    protected void startLogItem() {
        String name = this.rpTestItemName + " Console Log";
        String description = "";
        jobLogItemId = LaunchUtils.startTestItem(getLaunch().getRp(), rpTestItemId, name, description, null, "STEP");
    }

    @Override
    public Maybe<String> getJobLogItemId() {
        return jobLogItemId;
    }

    @Override
    public void setJobLogItemId(Maybe<String> jobLogItemId) {
        this.jobLogItemId = jobLogItemId;
    }

    @Override
    public void finish(Run run) {
        Launch rpLaunch = getLaunch().getRp();
        LaunchUtils.finishTestItem(rpLaunch, jobLogItemId, run);
        LaunchUtils.finishTestItem(rpLaunch, rpTestItemId, run, false);
    }

    @Override
    public ParentAware getParent() {
        return parent;
    }

    @Override
    public LaunchModel getLaunch() {
        ParentAware parent = getParent();
        while (!(parent instanceof LaunchModel)) {
            parent = parent.getParent();
        }
        return (LaunchModel) parent;
    }

    @Override
    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    @Override
    public void addDownStreamJobModel(DownStreamJobModel jobModel) {
        if (getDownStreamJobModelList() == null) {
            downStreamJobModelList = new ArrayList<>();
        }
        downStreamJobModelList.add(jobModel);
    }

    @Override
    public Maybe<String> getRpTestItemId() {
        return rpTestItemId;
    }

    public void setRpTestItemId(Maybe<String> rpTestItemId) {
        this.rpTestItemId = rpTestItemId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getRpTestItemName() {
        return rpTestItemName;
    }

    public void setRpTestItemName(String rpTestItemName) {
        this.rpTestItemName = rpTestItemName;
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

    public List<DownStreamJobModel> getDownStreamJobModelList() {
        return downStreamJobModelList;
    }

    public void setDownStreamJobModelList(List<DownStreamJobModel> downStreamJobModels) {
        this.downStreamJobModelList = downStreamJobModels;
    }

    @Override
    public String toString() {
        String parent = this.parent != null ? ((JobModel) this.parent).getJobName() : "";
        String description = this.description != null ? this.description : "";
        String jobName = this.jobName != null ? this.jobName : "";
        String run = this.run != null ? this.run.getFullDisplayName() : "";
        return "Job name: " + jobName + " ,Description: " + description + " ,Parent name: " + parent + " ,Run full name: " + run;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, description, tags, downStreamJobModelList, rpTestItemName, parent, run);
    }

    public Object clone() throws CloneNotSupportedException {
        JobModel newModel = (JobModel) super.clone();
        List<DownStreamJobModel> modelList = new ArrayList<>();
        newModel.setRpTestItemId(null);
        newModel.setRun(null);
        newModel.setJobLogItemId(null);
        if(getDownStreamJobModelList() != null) {
            for (DownStreamJobModel model : getDownStreamJobModelList()) {
                modelList.add((DownStreamJobModel) model.clone());
            }
        }
        newModel.setDownStreamJobModelList(modelList);
        return newModel;
    }

    protected Set<String> processTags(String delimitedString) {
        Set<String> tags = new HashSet<>();
        if(delimitedString != null) {
            for (String tag : delimitedString.split(SEMICOLON)) {
                tags.add(tag);
            }
        }
        return tags;
    }

    protected String getModelNameUsingPattern(Run run, String regex) {
        String name = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(run.getDisplayName());
        if (matcher.matches()) {
            name = matcher.group();
        }
        return name;
    }

}
