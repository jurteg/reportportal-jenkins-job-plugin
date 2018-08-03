package com.jurteg.jenkinsci.plugins.reportportal.runtimeutils;

import com.jurteg.jenkinsci.plugins.reportportal.Context;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ConfigModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.DownStreamJobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.ExecutableModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.JobModel;
import com.jurteg.jenkinsci.plugins.reportportal.plugin.model.LaunchModel;
import com.jurteg.jenkinsci.plugins.reportportal.runtime.RunnableModel;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RunUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(RunUtils.class);
    private static final Object RUN_MONITOR = new Object();

    public static void runExistingDownstreamJobsOrCreateSiblings(Run run, TaskListener listener) {
        boolean isReportingMoreThanOnce = false;
        List<LaunchModel> completeList = Context.getAllRunningLaunches();
        List<JobModel> existingModels = ModelUtils.getDownStreamJobsFromModelsByName(completeList, getRunJobName(run));
        for (JobModel jobModel : existingModels) {
            if (!jobModel.isClone() && ModelUtils.haveTheSameParent(jobModel, run)) {
                if (jobModel.getRpTestItemId() != null) {
                    if (!jobModel.getRun().getFullDisplayName().equals(run.getFullDisplayName())) {
                        JobModel clone = cloneModel(jobModel, run, listener);
                        if (clone != null) {
                            createRunnableAndExecute(clone, run);
                        }
                    }
                } else {
                    jobModel.setRun(run);
                    jobModel.setTaskListener(listener);
                    if (isReportingMoreThanOnce || isAlreadyRunningAsLaunch(jobModel, completeList)) {
                        JobModel temp = jobModel;
                        if (isModelWithTheSameNameAlreadyExistInParent(jobModel)) {
                            temp = cloneModel(jobModel, run, listener);
                        }
                        if (temp != null) {
                            createRunnableAndExecute(temp, run);
                        }
                    } else {
                        LOGGER.info(String.format("Starting Item '%s'. In thread '%s' / %s", jobModel.getComposedName(), Thread.currentThread().getName(), Thread.currentThread().getId()));
                        jobModel.start();
                        isReportingMoreThanOnce = true;
                    }
                }
            }
        }
    }

    private static boolean isModelWithTheSameNameAlreadyExistInParent(JobModel model) {
        JobModel parent = (JobModel) model.getParent();
        if (parent == null) {
            return false;
        }
        for (JobModel childModel : parent.getDownStreamJobModelList()) {
            if (childModel != model && childModel.getComposedName().equals(model.getComposedName())) {
                return true;
            }
        }
        return false;
    }

    private static JobModel cloneModel(JobModel source, Run run, TaskListener listener) {
        try {
            DownStreamJobModel newJobModel = (DownStreamJobModel) ((DownStreamJobModel) source).clone();
            newJobModel.setRun(run);
            newJobModel.setTaskListener(listener);
            ((JobModel) source.getParent()).addDownStreamJobModel(newJobModel);
            if (getRunJobName(newJobModel.getRun()).equals(getRunJobName(source.getRun())) && newJobModel.getComposedName().equals(source.getComposedName())) {
                newJobModel.addNameAttribute(String.format(" [%d]", getRunningClones(source).size() + 1));
            }
            return newJobModel;
        } catch (CloneNotSupportedException e) {
            LOGGER.error(String.format("Unable to clone test item with name '%s'.", source.getRpTestItemName()));
            return null;
        }
    }

    private static List<JobModel> getRunningClones(JobModel model) {
        List<JobModel> cloneList = new ArrayList<>();
        JobModel parent = (JobModel) model.getParent();
        for (JobModel jobModel : parent.getDownStreamJobModelList()) {
            if (getRunJobName(model.getRun()).equals(getRunJobName(jobModel.getRun())) && jobModel.isClone()) {
                cloneList.add(jobModel);
            }
        }
        return cloneList;
    }

    private static boolean isAlreadyRunningAsLaunch(JobModel model, List<LaunchModel> launchModelListToLook) {
        for (LaunchModel launch : launchModelListToLook) {
            if (launch.getUpstreamJobModel().getRun().equals(model.getRun())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAlreadyRunningAsItem(LaunchModel model, Run run, List<LaunchModel> launchModelListToLook) {
        List<JobModel> existingModels = ModelUtils.getDownStreamJobsFromModelsByName(launchModelListToLook, getRunJobName(run));
        for (JobModel job : existingModels) {
            if (model.getUpstreamJobModel().getRun().equals(job.getRun()) && job.getRpTestItemId() != null) {
                return true;
            }
        }
        return false;
    }

    private static void createRunnableAndExecute(ExecutableModel executable, Run run) {
        RunnableModel runnable = new RunnableModel(executable);
        Context.getExecutor().execute(runnable);
        synchronized (RUN_MONITOR) {
            while (!runnable.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        addNewRunnableModel(run, runnable);
    }

    private static void addNewRunnableModel(Run run, RunnableModel model) {
        if (Context.getRunnableModels(run) == null) {
            List<RunnableModel> emptyList = new ArrayList<>();
            Context.addRunnableModels(run, emptyList);
        }
        Context.addRunnableModel(run, model);
    }

    private static void finishRunnableModel(Run run, ExecutableModel model) {
        List<RunnableModel> itemsToRemove = new ArrayList<>();
        List<RunnableModel> runnableModelsList = Context.getRunnableModels(run);
        if (runnableModelsList != null) {
            try {
                for (RunnableModel runnable : runnableModelsList) {
                    if (runnable.getModel().equals(model)) {
                        itemsToRemove.add(runnable);
                        while (!runnable.done()) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    }
                }
            } finally {
                runnableModelsList.removeAll(itemsToRemove);
                if (Context.getRunnableModels(run) == null || Context.getRunnableModels(run).isEmpty()) {
                    Context.removeRunnableModels(run);
                }
            }
        }
    }

    public static void finishRunningDownStreamJobs(Run run) {
        for (JobModel jobModel : ModelUtils.getDownStreamJobsFromModelsByName(Context.getAllRunningLaunches(), getRunJobName(run))) {
            if (run.equals(jobModel.getRun()) && jobModel.getRpTestItemId() != null) { //running
                if (isRunnableModel(jobModel, run)) {
                    finishRunnableModel(run, jobModel);
                } else {
                    jobModel.finish();
                }
            }
        }
    }

    private static boolean isRunnableModel(ExecutableModel model, Run run) {
        List<RunnableModel> list = Context.getRunnableModels(run);
        if (list != null) {
            for (RunnableModel runnable : list) {
                if (runnable.getModel().equals(model)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void finishRunningLaunches(Run run, List<LaunchModel> launchesToFinish) {
        List<LaunchModel> completeList = new ArrayList<>();
        completeList.addAll(launchesToFinish);
        completeList.addAll(Context.getLaunchesFromRunnableList());
        try {
            for (LaunchModel model : completeList) {
                if (run.equals(model.getRun()) && model.getLaunch() != null) {
                    if (isRunnableModel(model, run)) {
                        finishRunnableModel(run, model);
                    } else {
                        model.finish();
                    }
                }
            }
        } finally {
            Context.removeRunnableModels(run);
        }
    }

    public static List<LaunchModel> runNewLaunches(Run run, TaskListener listener, ConfigModel config) {
        List<LaunchModel> newLaunchesToRun = new ArrayList<>();
        boolean hasSibling = false;
        for (LaunchModel model : ModelUtils.getLaunchModelListToRun(run, listener, config)) {
            if (!isAlreadyRunningInList(model, Context.getAllRunningLaunches())) {
                if (model.isReportingEnabled()) {
                    if (!hasSibling && !isAlreadyRunningAsItem(model, run, Context.getRunningLaunches())) {
                        LOGGER.info(String.format("Starting Launch '%s'. In thread '%s' / %s", model.getComposedName(), Thread.currentThread().getName(), Thread.currentThread().getId()));
                        model.start();
                        newLaunchesToRun.add(model);
                        hasSibling = true;
                    } else {
                        createRunnableAndExecute(model, run);
                    }
                }
            }
        }
        return newLaunchesToRun;
    }

    public static String getRunJobName(Run run) {
        return run.getFullDisplayName().replace(run.getSearchName(), "").trim();
    }

    public static boolean isAlreadyRunningInList(LaunchModel launchModel, List<LaunchModel> launchModelListToCompareWith) {
        for (LaunchModel model : launchModelListToCompareWith) {
            if (model.equals(launchModel)) {
                return true;
            }
        }
        return false;
    }

}
